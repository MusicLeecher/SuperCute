/*
	SuperCollider real time audio synthesis system
    Copyright (c) 2002 James McCartney. All rights reserved.
	http://www.audiosynth.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


#include "SC_Graph.h"
#include "SC_GraphDef.h"
#include "SC_Unit.h"
#include "SC_UnitSpec.h"
#include "SC_UnitDef.h"
#include "SC_HiddenWorld.h"
#include "SC_Wire.h"
#include "SC_WireSpec.h"
#include <stdio.h>
#include <string.h>
#include "SC_Prototypes.h"
#include "SC_Errors.h"
#include "Unroll.h"

#ifdef SC_WIN32
// workaround for IN/OUT conflict with Win32 headers. see SC_Unit.h for details
#define IN SC_IN
#define OUT SC_OUT
#endif

void Unit_ChooseMulAddFunc(Unit* unit);

////////////////////////////////////////////////////////////////////////////////

void Graph_FirstCalc(Graph *inGraph);

void Graph_Dtor(Graph *inGraph)
{
	//scprintf("->Graph_Dtor %d\n", inGraph->mNode.mID);
	World *world = inGraph->mNode.mWorld;
	int numUnits = inGraph->mNumUnits;
	Unit** graphUnits = inGraph->mUnits;
	if (inGraph->mNode.mCalcFunc != (NodeCalcFunc)Graph_FirstCalc) {
		// the above test insures that dtors are not called if ctors have not been called.
		for (int i = 0; i<numUnits; ++i) {
			Unit *unit = graphUnits[i];
			UnitDtorFunc dtor = unit->mUnitDef->mUnitDtorFunc;
			if (dtor) (dtor)(unit);
		}
	}
	world->mNumUnits -= numUnits;
	world->mNumGraphs --;
	
	GraphDef* def = GRAPHDEF(inGraph);
	if (--def->mRefCount <= 0) {
		if (world->mRealTime) GraphDef_DeleteMsg(world, def);
		else GraphDef_Free(def);
	}
	
	Node_Dtor(&inGraph->mNode);
	//scprintf("<-Graph_Dtor\n");
}

////////////////////////////////////////////////////////////////////////////////

int Graph_New(struct World *inWorld, struct GraphDef *inGraphDef, int32 inID, 
			struct sc_msg_iter* args, Graph** outGraph)
{
	Graph* graph;
	int err = Node_New(inWorld, &inGraphDef->mNodeDef, inID, (Node**)&graph);
	if (err) return err;
	Graph_Ctor(inWorld, inGraphDef, graph, args);
	*outGraph = graph;
	return err;
}

void Graph_Ctor(World *inWorld, GraphDef *inGraphDef, Graph *graph, sc_msg_iter *msg)
{	
	//scprintf("->Graph_Ctor\n");
	
	// hit the memory allocator only once.
	char *memory = (char*)graph + sizeof(Graph);
	
	// allocate space for children
	int numUnits = inGraphDef->mNumUnitSpecs;
	graph->mNumUnits = numUnits;
	inWorld->mNumUnits += numUnits;
	inWorld->mNumGraphs ++;	
	
	graph->mUnits = (Unit**)memory; 
	memory += inGraphDef->mUnitsAllocSize;

	// set calc func
	graph->mNode.mCalcFunc = (NodeCalcFunc)&Graph_FirstCalc;
	
	// allocate wires
	graph->mNumWires = inGraphDef->mNumWires;
	graph->mWire = (Wire*)memory;
	memory += inGraphDef->mWiresAllocSize;

	graph->mNumCalcUnits = inGraphDef->mNumCalcUnits;
	graph->mCalcUnits = (Unit**)memory;
	memory += inGraphDef->mCalcUnitsAllocSize;
	
	// initialize controls
	int numControls = inGraphDef->mNumControls;
	graph->mNumControls = numControls;
	graph->mControls = (float*)memory;
	memory += inGraphDef->mControlAllocSize;
	
	graph->mMapControls = (float**)memory;
	memory += inGraphDef->mMapControlsAllocSize;
	
	{
		float*  graphControls = graph->mControls;
		float*  initialControlValues = inGraphDef->mInitialControlValues;
		float** graphMapControls = graph->mMapControls;
		for (int i=0; i<numControls; ++i, ++graphControls) {
			*graphControls = initialControlValues[i];
			graphMapControls[i] = graphControls;
		}
	}
	
	// set controls
	while (msg->remain() >= 8) {
		if (msg->nextTag('i') == 's') {
			int32* name = msg->gets4();
			int32 hash = Hash(name);
			if (msg->nextTag('f') == 's') {
				char* string = msg->gets();
				if (*string == 'c') {
					int bus = 0, c;
					string++;
					while ((c = *string++) != 0) {
						if (c >= '0' && c <= '9') bus = bus * 10 + c - '0';
					}
					Graph_MapControl(graph, hash, name, 0, bus);
				}
			} else {
				float32 value = msg->getf();
				Graph_SetControl(graph, hash, name, 0, value);
			}
		} else {
			int32 index = msg->geti();
			if (msg->nextTag('f') == 's') {
				char* string = msg->gets();
				if (*string == 'c') {
					int bus = 0, c;
					string++;
					while ((c = *string++) != 0) {
						if (c >= '0' && c <= '9') bus = bus * 10 + c - '0';
					}
					Graph_MapControl(graph, index, bus);
				}
			} else {
				float32 value = msg->getf();
				Graph_SetControl(graph, index, value);
			}
		}
	}

	// set up scalar values
	Wire *graphWires = graph->mWire;
	int numConstants = inGraphDef->mNumConstants;
	{
		float *constants = inGraphDef->mConstants;
		Wire *wire = graphWires;
		for (int i=0; i<numConstants; ++i, ++wire) {
			wire->mFromUnit = 0;
			wire->mCalcRate = calc_ScalarRate;
			wire->mBuffer = &wire->mScalarValue;
			wire->mScalarValue = constants[i];
		}
	}
	
	graph->mSampleOffset = inWorld->mSampleOffset;
	graph->mRGen = inWorld->mRGen; // defaults to rgen zero.
	
	// initialize units
	//scprintf("initialize units\n");
	Unit** calcUnits = graph->mCalcUnits;
	Unit** graphUnits = graph->mUnits;
	int calcCtr=0;

	float *bufspace = inWorld->hw->mWireBufSpace;
	int wireCtr = numConstants;
	UnitSpec *unitSpec = inGraphDef->mUnitSpecs;
	for (int i=0; i<numUnits; ++i, ++unitSpec) {
		// construct unit from spec
		Unit *unit = Unit_New(inWorld, unitSpec, memory);
		
		// set parent
		unit->mParent = graph;
		unit->mParentIndex = i;
		
		graphUnits[i] = unit;
		
		{
			// hook up unit inputs
			//scprintf("hook up unit inputs\n");
			InputSpec *inputSpec = unitSpec->mInputSpec;
			Wire** unitInput = unit->mInput;
			float** unitInBuf = unit->mInBuf;
			int numInputs = unitSpec->mNumInputs;
			for (int j=0; j<numInputs; ++j, ++inputSpec) {			
				Wire *wire = graphWires + inputSpec->mWireIndex;
				unitInput[j] = wire;
				unitInBuf[j] = wire->mBuffer;
			}
		}
		
		{
			// hook up unit outputs
			//scprintf("hook up unit outputs\n");
			Wire** unitOutput = unit->mOutput;
			float** unitOutBuf = unit->mOutBuf;
			int numOutputs = unitSpec->mNumOutputs;
			Wire *wire = graphWires + wireCtr;
			wireCtr += numOutputs;
			int unitCalcRate = unit->mCalcRate;
			if (unitCalcRate == calc_FullRate) {
				OutputSpec *outputSpec = unitSpec->mOutputSpec;
				for (int j=0; j<numOutputs; ++j, ++wire, ++outputSpec) {
					wire->mFromUnit = unit;
					wire->mCalcRate = calc_FullRate;
					wire->mBuffer = bufspace + outputSpec->mBufferIndex;
					unitOutput[j] = wire;
					unitOutBuf[j] = wire->mBuffer;
				}
				calcUnits[calcCtr++] = unit;
			} else {
				for (int j=0; j<numOutputs; ++j, ++wire) {
					wire->mFromUnit = unit;
					wire->mCalcRate = unitCalcRate;
					wire->mBuffer = &wire->mScalarValue;
					unitOutput[j] = wire;
					unitOutBuf[j] = wire->mBuffer;
				}
				if (unitCalcRate != calc_ScalarRate) {
					calcUnits[calcCtr++] = unit;
				}
			}
		}
	}

	inGraphDef->mRefCount ++ ;
}

void Graph_RemoveID(World* inWorld, Graph *inGraph)
{
	if (!World_RemoveNode(inWorld, &inGraph->mNode)) {
		int err = kSCErr_Failed; // shouldn't happen..
		throw err;
	}

	HiddenWorld* hw = inWorld->hw;
	int id = hw->mHiddenID = (hw->mHiddenID - 8) | 0x80000000;
	inGraph->mNode.mID = id;
	inGraph->mNode.mHash = Hash(id);
    if (!World_AddNode(inWorld, &inGraph->mNode)) {
		scprintf("mysterious failure in Graph_RemoveID\n");
		Node_Delete(&inGraph->mNode);
		// enums are uncatchable. must throw an int.
		int err = kSCErr_Failed; // shouldn't happen..
		throw err;
    }
	
	//inWorld->hw->mRecentID = id;
}

void Graph_FirstCalc(Graph *inGraph)
{
	//scprintf("->Graph_FirstCalc\n");
	int numUnits = inGraph->mNumUnits;
	Unit **units = inGraph->mUnits;
	for (int i=0; i<numUnits; ++i) {
		Unit *unit = units[i];
		// call constructor
		(*unit->mUnitDef->mUnitCtorFunc)(unit);
	}
	//scprintf("<-Graph_FirstCalc\n");
	
	inGraph->mNode.mCalcFunc = (NodeCalcFunc)&Graph_Calc;
	// now do actual graph calculation
	Graph_Calc(inGraph);
}

void Node_NullCalc(struct Node* /*inNode*/);

void Graph_NullFirstCalc(Graph *inGraph)
{
	//scprintf("->Graph_FirstCalc\n");
	int numUnits = inGraph->mNumUnits;
	Unit **units = inGraph->mUnits;
	for (int i=0; i<numUnits; ++i) {
		Unit *unit = units[i];
		// call constructor
		(*unit->mUnitDef->mUnitCtorFunc)(unit);
	}
	//scprintf("<-Graph_FirstCalc\n");
	
	inGraph->mNode.mCalcFunc = &Node_NullCalc;
}

void Graph_Calc(Graph *inGraph)
{
	//scprintf("->Graph_Calc\n");
	int numCalcUnits = inGraph->mNumCalcUnits;
	Unit **calcUnits = inGraph->mCalcUnits;
	for (int i=0; i<numCalcUnits; ++i) {
		Unit *unit = calcUnits[i];
		(unit->mCalcFunc)(unit, unit->mBufLength);
	}
	//scprintf("<-Graph_Calc\n");
}

void Graph_CalcTrace(Graph *inGraph);
void Graph_CalcTrace(Graph *inGraph)
{
	int numCalcUnits = inGraph->mNumCalcUnits;
	Unit **calcUnits = inGraph->mCalcUnits;
	scprintf("\nTRACE %d  %s    num ugens %d\n", inGraph->mNode.mID, inGraph->mNode.mDef->mName, numCalcUnits);
	for (int i=0; i<numCalcUnits; ++i) {
		Unit *unit = calcUnits[i];
		scprintf("  unit %d %s\n    in ", i, (char*)unit->mUnitDef->mUnitDefName);
		for (int j=0; j<unit->mNumInputs; ++j) {
			scprintf(" %g", ZIN0(j));
		}
		scprintf("\n");
		(unit->mCalcFunc)(unit, unit->mBufLength);
		scprintf("    out");
		for (int j=0; j<unit->mNumOutputs; ++j) {
			scprintf(" %g", ZOUT0(j));
		}
		scprintf("\n");
	}
	inGraph->mNode.mCalcFunc = (NodeCalcFunc)&Graph_Calc;
}

void Graph_Trace(Graph *inGraph)
{
	if (inGraph->mNode.mCalcFunc == (NodeCalcFunc)&Graph_Calc) {
		inGraph->mNode.mCalcFunc = (NodeCalcFunc)&Graph_CalcTrace;
	}
}


int Graph_GetControl(Graph* inGraph, uint32 inIndex, float& outValue)
{
	if (inIndex >= GRAPHDEF(inGraph)->mNumControls) return kSCErr_IndexOutOfRange;
	outValue = inGraph->mControls[inIndex];
	return kSCErr_None;
}

int Graph_GetControl(Graph* inGraph, int32 inHash, int32 *inName, uint32 inIndex, float& outValue)
{
	ParamSpecTable* table = GRAPH_PARAM_TABLE(inGraph);
	ParamSpec *spec = table->Get(inHash, inName);
	if (!spec) return kSCErr_IndexOutOfRange;
	return Graph_GetControl(inGraph, spec->mIndex + inIndex, outValue);
}

void Graph_SetControl(Graph* inGraph, uint32 inIndex, float inValue)
{
	if (inIndex >= GRAPHDEF(inGraph)->mNumControls) return;
	inGraph->mControls[inIndex] = inValue;
}

void Graph_SetControl(Graph* inGraph, int32 inHash, int32 *inName, uint32 inIndex, float inValue)
{
	ParamSpecTable* table = GRAPH_PARAM_TABLE(inGraph);
	ParamSpec *spec = table->Get(inHash, inName);
	if (spec) Graph_SetControl(inGraph, spec->mIndex + inIndex, inValue);
}



void Graph_MapControl(Graph* inGraph, int32 inHash, int32 *inName, uint32 inIndex, uint32 inBus)
{
	ParamSpecTable* table = GRAPH_PARAM_TABLE(inGraph);
	ParamSpec *spec = table->Get(inHash, inName);
	if (spec) Graph_MapControl(inGraph, spec->mIndex + inIndex, inBus);
}

void Graph_MapControl(Graph* inGraph, uint32 inIndex, uint32 inBus)
{
	if (inIndex >= GRAPHDEF(inGraph)->mNumControls) return;
	World *world = inGraph->mNode.mWorld;
	if (inBus == 0xFFFFFFFF) {
		inGraph->mMapControls[inIndex] = inGraph->mControls + inIndex;
	} else if (inBus < world->mNumControlBusChannels) {
		inGraph->mMapControls[inIndex] = world->mControlBus + inBus;
	}
}

