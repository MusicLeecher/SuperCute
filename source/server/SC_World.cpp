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


#include "SC_World.h"
#include "SC_WorldOptions.h"
#include "SC_HiddenWorld.h"
#include "SC_InterfaceTable.h"
#include "SC_AllocPool.h"
#include "SC_GraphDef.h"
#include "SC_UnitDef.h"
#include "SC_BufGen.h"
#include "SC_Node.h"
#include "SC_CoreAudio.h"
#include "SC_Group.h"
#include "SC_Errors.h"
#include <stdio.h>
#include "SC_Prototypes.h"
#include "SC_Samp.h"
#include "SC_ComPort.h"

InterfaceTable gInterfaceTable;
PrintFunc gPrint = 0;

extern HashTable<struct UnitDef, Malloc> *gUnitDefLib;
extern HashTable<struct BufGen, Malloc> *gBufGenLib;
extern HashTable<PlugInCmd, Malloc> *gPlugInCmds;

extern "C" {
int sndfileFormatInfoFromStrings(struct SF_INFO *info, 
	const char *headerFormatString, const char *sampleFormatString);
bool SendMsgToEngine(World *inWorld, FifoMsg& inMsg);
bool SendMsgFromEngine(World *inWorld, FifoMsg& inMsg);
}

////////////////////////////////////////////////////////////////////////////////

void InterfaceTable_Init();
void InterfaceTable_Init()
{
	InterfaceTable *ft = &gInterfaceTable;
	
	ft->mSine = gSine;
	ft->mSecant = gInvSine;
	ft->mSineSize = kSineSize;
	ft->mSineWavetable = gSineWavetable;
	
	ft->fPrint = &scprintf;
	
	ft->fRanSeed = &timeseed;

	ft->fNodeEnd = &Node_End;
	
	ft->fDefineUnit = &UnitDef_Create;
	ft->fDefineBufGen = &BufGen_Create;
	ft->fClearUnitOutputs = &Unit_ZeroOutputs;

	ft->fNRTAlloc = &malloc;
	ft->fNRTRealloc = &realloc;
	ft->fNRTFree = &free;

	ft->fRTAlloc = &World_Alloc;
	ft->fRTRealloc = &World_Realloc;
	ft->fRTFree = &World_Free;
	
	ft->fNodeRun = &Node_SetRun;
	
	ft->fSendTrigger = &Node_SendTrigger;
	
	ft->fDefineUnitCmd = &UnitDef_AddCmd;
	ft->fDefinePlugInCmd = &PlugIn_DefineCmd;
	
	ft->fSendMsgFromRT = &SendMsgFromEngine;
	ft->fSendMsgToRT = &SendMsgToEngine;
	
	ft->fSndFileFormatInfoFromStrings = &sndfileFormatInfoFromStrings;
	
	ft->fGetNode = &World_GetNode;
	ft->fGetGraph = &World_GetGraph;
	
	ft->fNRTLock = &World_NRTLock;
	ft->fNRTUnlock = &World_NRTUnlock;
		
#if __VEC__ 
	long response;
	Gestalt(gestaltPowerPCProcessorFeatures, &response);
	if (response & (1<<gestaltPowerPCHasVectorInstructions)) ft->mAltivecAvailable = true;
	else ft->mAltivecAvailable = false;
#else
	ft->mAltivecAvailable = false;
#endif

	ft->fGroup_DeleteAll = &Group_DeleteAll;
	ft->fDoneAction = &Unit_DoneAction;

}

void initialize_library();
void initializeScheduler();

World* World_New(WorldOptions *inOptions)
{	
	World *world = 0;
	
	try {
		static bool gLibInitted = false;
		if (!gLibInitted) {
			InterfaceTable_Init();
			initialize_library();
			initializeScheduler();
			gLibInitted = true;
		}
	
		world = (World*)calloc(sizeof(World), 1);
		
		world->hw = (HiddenWorld*)calloc(sizeof(HiddenWorld), 1);

		world->hw->mAllocPool = new AllocPool(malloc, free, inOptions->mRealTimeMemorySize * 1024, 0);
		world->hw->mQuitProgram = new SC_Semaphore(0);
	
		extern Malloc gMalloc;
	
		HiddenWorld *hw = world->hw;
		hw->mGraphDefLib = new HashTable<GraphDef, Malloc>(&gMalloc, inOptions->mMaxGraphDefs, false);
		hw->mNodeLib = new IntHashTable<Node, AllocPool>(hw->mAllocPool, inOptions->mMaxNodes, false);
		hw->mUsers = (ReplyAddress*)calloc(inOptions->mMaxLogins * sizeof(ReplyAddress), 1);
		hw->mNumUsers = 0;
		hw->mMaxUsers = inOptions->mMaxLogins;
		
		world->mNumUnits = 0;
		world->mNumGraphs = 0;
		world->mNumGroups = 0;
	
		world->mBufCounter = 0;
		world->mBufLength = inOptions->mBufLength;
		world->mSampleOffset = 0;
		world->mNumAudioBusChannels = inOptions->mNumAudioBusChannels;
		world->mNumControlBusChannels = inOptions->mNumControlBusChannels;
		world->mNumInputs = inOptions->mNumInputBusChannels;
		world->mNumOutputs = inOptions->mNumOutputBusChannels;
		
		world->mNumSharedSndBufs = inOptions->mNumSharedSndBufs;
		world->mSharedSndBufs = inOptions->mSharedSndBufs;
		
		world->mNumSharedControls = inOptions->mNumSharedControls;
		world->mSharedControls = inOptions->mSharedControls;

		int numsamples = world->mBufLength * world->mNumAudioBusChannels;
		world->mAudioBus = (float*)calloc(numsamples, sizeof(float));

		world->mControlBus = (float*)calloc(world->mNumControlBusChannels, sizeof(float));
		world->mSharedControls = (float*)calloc(world->mNumSharedControls, sizeof(float));
		
		world->mAudioBusTouched = (int32*)calloc(inOptions->mNumAudioBusChannels, sizeof(int32));
		world->mControlBusTouched = (int32*)calloc(inOptions->mNumControlBusChannels, sizeof(int32));
		
		world->mNumSndBufs = inOptions->mNumBuffers;
		world->mSndBufs = (SndBuf*)calloc(world->mNumSndBufs, sizeof(SndBuf));
		world->mSndBufsNonRealTimeMirror = (SndBuf*)calloc(world->mNumSndBufs, sizeof(SndBuf));
				
		GroupNodeDef_Init();
		
		world->mTopGroup = Group_New(world, 0);
	
		world->mRealTime = inOptions->mRealTime;
		
		world->ft = &gInterfaceTable;
		
		world->mNumRGens = inOptions->mNumRGens;
		world->mRGen = new RGen[world->mNumRGens];
		for (int i=0; i<world->mNumRGens; ++i) {
			world->mRGen[i].init(timeseed());
		}
		
		world->mNRTLock = new SC_Lock();
		
		if (inOptions->mPassword) {
			strncpy(world->hw->mPassword, inOptions->mPassword, 31);
			world->hw->mPassword[31] = 0;
		} else {
			strcpy(world->hw->mPassword, "go");
		}
		
		hw->mMaxWireBufs = inOptions->mMaxWireBufs;
		hw->mWireBufSpace = 0;
	
		if (world->mRealTime) {
			hw->mAudioDriver = new SC_CoreAudioDriver(world);
		
			GraphDef *list = 0;
			list = GraphDef_LoadDir(world, "synthdefs", list);
			GraphDef_Define(world, list);
		
			if (!hw->mAudioDriver->Setup()) {
				scprintf("could not initialize audio.\n");
				return 0;
			}
			if (!hw->mAudioDriver->Start()) {
				scprintf("start audio failed.\n");
				return 0;
			}
		} else {		
			GraphDef *list = 0;
			list = GraphDef_LoadDir(world, "synthdefs", list);
			GraphDef_Define(world, list);

			// batch process non real time audio
			if (!inOptions->mNonRealTimeCmdFilename)
				throw std::runtime_error("Non real time command filename is NULL.\n");
				
			if (!inOptions->mNonRealTimeOutputFilename) 
				throw std::runtime_error("Non real time output filename is NULL.\n");
			
			SF_INFO info;
			info.samplerate = inOptions->mNonRealTimeSampleRate;
			info.channels = world->mNumOutputs;
			world->hw->mNRTOutputFile = sf_open(inOptions->mNonRealTimeOutputFilename, SFM_WRITE, &info);
			if (!world->hw->mNRTOutputFile) 
				throw std::runtime_error("Couldn't open non real time output file.\n");
			
			if (inOptions->mNonRealTimeInputFilename) {
				world->hw->mNRTInputFile = sf_open(inOptions->mNonRealTimeInputFilename, SFM_READ, &info);
				if (!world->hw->mNRTInputFile) 
					throw std::runtime_error("Couldn't open non real time input file.\n");
				
				if (info.samplerate != inOptions->mNonRealTimeSampleRate)
					scprintf("WARNING: input file sample rate does not equal output sample rate.\n");
					
			} else {
				world->hw->mNRTInputFile = 0;
			}
			
			world->hw->mNRTCmdFile = fopen(inOptions->mNonRealTimeCmdFilename, "r");
			if (!world->hw->mNRTCmdFile) 
				throw std::runtime_error("Couldn't open non real time command file.\n");
			
		}
	} catch (std::exception& exc) {
		scprintf("Exception in World_New: %s\n", exc.what());
		World_Cleanup(world); 
		return 0;
	} catch (...) {
	}
	return world;
}

void World_OpenUDP(struct World *inWorld, int inPort)
{
	try {
		new SC_UdpInPort(inWorld, inPort);
	} catch (std::exception& exc) {
		scprintf("Exception in World_OpenUDP: %s\n", exc.what());
	} catch (...) {
	}
}

void World_OpenTCP(struct World *inWorld, int inPort, int inMaxConnections, int inBacklog)
{
	try {
		new SC_TcpInPort(inWorld, inPort, inMaxConnections, inBacklog);
	} catch (std::exception& exc) {
		scprintf("Exception in World_OpenTCP: %s\n", exc.what());
	} catch (...) {
	}
}

void World_WaitForQuit(struct World *inWorld)
{
	try {
		inWorld->hw->mQuitProgram->Acquire();
		World_Cleanup(inWorld);
	} catch (std::exception& exc) {
		scprintf("Exception in World_WaitForQuit: %s\n", exc.what());
	} catch (...) {
	}
}

void World_SetSampleRate(World *inWorld, double inSampleRate)
{
	inWorld->mSampleRate = inSampleRate;
	Rate_Init(&inWorld->mFullRate, inSampleRate, inWorld->mBufLength);
	Rate_Init(&inWorld->mBufRate, inSampleRate / inWorld->mBufLength, 1);
}

////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////

void* World_Alloc(World *inWorld, size_t inByteSize)
{
	return inWorld->hw->mAllocPool->Alloc(inByteSize);
}

void* World_Realloc(World *inWorld, void *inPtr, size_t inByteSize)
{
	return inWorld->hw->mAllocPool->Realloc(inPtr, inByteSize);
}

size_t World_TotalFree(World *inWorld)
{
	return inWorld->hw->mAllocPool->TotalFree();
}

size_t World_LargestFreeChunk(World *inWorld)
{
	return inWorld->hw->mAllocPool->LargestFreeChunk();
}

void World_Free(World *inWorld, void *inPtr)
{
	inWorld->hw->mAllocPool->Free(inPtr);
}

////////////////////////////////////////////////////////////////////////////////

int32 *GetKey(GraphDef *inGraphDef)
{
	return inGraphDef->mNodeDef.mName;
}

int32 GetHash(GraphDef *inGraphDef)
{
	return inGraphDef->mNodeDef.mHash;
}

bool World_AddGraphDef(World *inWorld, GraphDef* inGraphDef)
{
	return inWorld->hw->mGraphDefLib->Add(inGraphDef);
}

bool World_RemoveGraphDef(World *inWorld, GraphDef* inGraphDef)
{
	bool res =  inWorld->hw->mGraphDefLib->Remove(inGraphDef);
	return res;
}

bool World_FreeGraphDef(World *inWorld, GraphDef* inGraphDef)
{
	bool res =  inWorld->hw->mGraphDefLib->Remove(inGraphDef);
	if (res) GraphDef_Free(inGraphDef);
	return res;
}

void World_FreeAllGraphDefs(World *inWorld)
{
	HashTable<struct GraphDef, Malloc>* lib = inWorld->hw->mGraphDefLib;
	int size = lib->TableSize();
	for (int i=0; i<size; ++i) {
		GraphDef *def = lib->AtIndex(i);
		if (def) GraphDef_Free(def);
	}
	lib->MakeEmpty();
}

GraphDef* World_GetGraphDef(World *inWorld, int32* inKey)
{
	return inWorld->hw->mGraphDefLib->Get(inKey);
}

////////////////////////////////////////////////////////////////////////////////

int32 *GetKey(UnitDef *inUnitDef)
{
	return inUnitDef->mUnitDefName;
}

int32 GetHash(UnitDef *inUnitDef)
{
	return inUnitDef->mHash;
}

bool AddUnitDef(UnitDef* inUnitDef)
{
	return gUnitDefLib->Add(inUnitDef);
}

bool RemoveUnitDef(UnitDef* inUnitDef)
{
	return gUnitDefLib->Remove(inUnitDef);
}

UnitDef* GetUnitDef(int32* inKey)
{
	return gUnitDefLib->Get(inKey);
}

////////////////////////////////////////////////////////////////////////////////

int32 *GetKey(BufGen *inBufGen)
{
	return inBufGen->mBufGenName;
}

int32 GetHash(BufGen *inBufGen)
{
	return inBufGen->mHash;
}

bool AddBufGen(BufGen* inBufGen)
{
	return gBufGenLib->Add(inBufGen);
}

bool RemoveBufGen(BufGen* inBufGen)
{
	return gBufGenLib->Remove(inBufGen);
}

BufGen* GetBufGen(int32* inKey)
{
	return gBufGenLib->Get(inKey);
}

////////////////////////////////////////////////////////////////////////////////

int32 *GetKey(PlugInCmd *inPlugInCmd)
{
	return inPlugInCmd->mCmdName;
}

int32 GetHash(PlugInCmd *inPlugInCmd)
{
	return inPlugInCmd->mHash;
}

bool AddPlugInCmd(PlugInCmd* inPlugInCmd)
{
	return gPlugInCmds->Add(inPlugInCmd);
}

bool RemovePlugInCmd(PlugInCmd* inPlugInCmd)
{
	return gPlugInCmds->Remove(inPlugInCmd);
}

PlugInCmd* GetPlugInCmd(int32* inKey)
{
	return gPlugInCmds->Get(inKey);
}

////////////////////////////////////////////////////////////////////////////////

int32 GetKey(Node *inNode)
{
	return inNode->mID;
}

int32 GetHash(Node *inNode)
{
	return inNode->mHash;
}

bool World_AddNode(World *inWorld, Node* inNode)
{
	return inWorld->hw->mNodeLib->Add(inNode);
}

bool World_RemoveNode(World *inWorld, Node* inNode)
{
	return inWorld->hw->mNodeLib->Remove(inNode);
}

Node* World_GetNode(World *inWorld, int32 inID)
{
	return inWorld->hw->mNodeLib->Get(inID);
}

Graph* World_GetGraph(World *inWorld, int32 inID)
{
	Node *node = World_GetNode(inWorld, inID);
	if (!node) return 0;
	return node->mIsGroup ? 0 : (Graph*)node;
}

Group* World_GetGroup(World *inWorld, int32 inID)
{
	Node *node = World_GetNode(inWorld, inID);
	if (!node) return 0;
	return node->mIsGroup ? (Group*)node : 0;
}

////////////////////////////////////////////////////////////////////////////////

void World_Run(World *inWorld)
{
	// run top group
	Group_Calc(inWorld->mTopGroup);
}

void World_Start(World *inWorld)
{
	inWorld->mBufCounter = 0;
	for (int i=0; i<inWorld->mNumAudioBusChannels; ++i) inWorld->mAudioBusTouched[i] = -1;
	for (int i=0; i<inWorld->mNumControlBusChannels; ++i) inWorld->mControlBusTouched[i] = -1;
	
	inWorld->hw->mWireBufSpace = (float*)malloc(inWorld->hw->mMaxWireBufs * inWorld->mBufLength * sizeof(float));
	
	inWorld->hw->mTriggers.MakeEmpty();
	inWorld->hw->mNodeEnds.MakeEmpty();
}

void World_Cleanup(World *world)
{
	if (!world) return;
	
	HiddenWorld *hw = world->hw;
	
	if (hw) hw->mAudioDriver->Stop();

	if (world->mTopGroup) Group_DeleteAll(world->mTopGroup);
	
	if (hw) {
		free(hw->mWireBufSpace);
		delete hw->mAudioDriver;
	}
	delete world->mNRTLock;
	World_Free(world, world->mTopGroup);
	
	for (int i=0; i<world->mNumSndBufs; ++i) {
		SndBuf *nrtbuf = world->mSndBufsNonRealTimeMirror + i;
		SndBuf * rtbuf = world->mSndBufs + i;
		
		if (nrtbuf->data) free(nrtbuf->data);
		if (rtbuf->data && rtbuf->data != nrtbuf->data) free(rtbuf->data);
		
		if (nrtbuf->sndfile) sf_close(nrtbuf->sndfile);
		if (rtbuf->sndfile && rtbuf->sndfile != nrtbuf->sndfile) sf_close(rtbuf->sndfile);
	}
		
	free(world->mSndBufsNonRealTimeMirror);
	free(world->mSndBufs);
	
	free(world->mControlBusTouched);
	free(world->mAudioBusTouched);
	free(world->mControlBus);
	free(world->mAudioBus);
	delete [] world->mRGen;
	if (hw) {
		free(hw->mUsers);
		delete hw->mNodeLib;
		delete hw->mGraphDefLib;
		delete hw->mQuitProgram;
		delete hw->mAllocPool;
		free(hw);
	}
	free(world);
}


void World_NRTLock(World *world)
{
	world->mNRTLock->Lock();
}

void World_NRTUnlock(World *world)
{
	world->mNRTLock->Unlock();
}

////////////////////////////////////////////////////////////////////////////////


inline int32 BUFMASK(int32 x)
{
	return (1 << (31 - CLZ(x))) - 1;
}

SCErr bufAlloc(SndBuf* buf, int numChannels, int numFrames)
{		
	long numSamples = numFrames * numChannels;
	size_t size = numSamples * sizeof(float);
	
	buf->data = (float*)calloc(size, 1);
	if (!buf->data) return kSCErr_Failed;
	
	buf->channels = numChannels;
	buf->frames   = numFrames;
	buf->samples  = numSamples;
	buf->mask     = BUFMASK(numSamples); // for delay lines
	buf->mask1    = buf->mask - 1;	// for oscillators

	return kSCErr_None;
}

int sampleFormatFromString(const char* name);
int sampleFormatFromString(const char* name)
{		
	size_t len = strlen(name);
	if (len < 1) return 0;
	
	if (name[0] == 'u') {
		if (len < 4) return 0;
		if (name[4] == '8') return SF_FORMAT_PCM_U8;
		return 0;
	} else if (name[0] == 'i') {
		if (len < 4) return 0;
		if (name[3] == '8') return SF_FORMAT_PCM_S8;
		else if (name[3] == '1') return SF_FORMAT_PCM_16;
		else if (name[3] == '2') return SF_FORMAT_PCM_24;
		else if (name[3] == '3') return SF_FORMAT_PCM_32;
	} else if (name[0] == 'f') {
		return SF_FORMAT_FLOAT;
	} else if (name[0] == 'd') {
		return SF_FORMAT_DOUBLE;
	} else if (name[0] == 'm') {
		return SF_FORMAT_ULAW;
	} else if (name[0] == 'a') {
		return SF_FORMAT_ALAW;
	}
	return 0;
}

int headerFormatFromString(const char *name);
int headerFormatFromString(const char *name)
{
	if (strcasecmp(name, "AIFF")==0) return SF_FORMAT_AIFF;
	if (strcasecmp(name, "AIFC")==0) return SF_FORMAT_AIFF;
	if (strcasecmp(name, "RIFF")==0) return SF_FORMAT_WAV;
	if (strcasecmp(name, "WAVE")==0) return SF_FORMAT_WAV;
	if (strcasecmp(name, "WAV" )==0) return SF_FORMAT_WAV;
	if (strcasecmp(name, "Sun" )==0) return SF_FORMAT_AU;
	if (strcasecmp(name, "IRCAM")==0) return SF_FORMAT_IRCAM;
	if (strcasecmp(name, "NeXT")==0) return SF_FORMAT_AU;
	if (strcasecmp(name, "raw")==0) return SF_FORMAT_RAW;
	return 0;
}

int sndfileFormatInfoFromStrings(struct SF_INFO *info, const char *headerFormatString, const char *sampleFormatString)
{
	int headerFormat = headerFormatFromString(headerFormatString);
	if (!headerFormat) return kSCErr_Failed;
	
	int sampleFormat = sampleFormatFromString(sampleFormatString);
	if (!sampleFormat) return kSCErr_Failed;
	
	info->format = (unsigned int)(headerFormat | sampleFormat);
	return kSCErr_None;
}

#include "scsynthsend.h"

void TriggerMsg::Perform()
{
	scpacket packet;
	packet.adds("/tr");
	packet.maketags(4);
	packet.addtag(',');
	packet.addtag('i');
	packet.addtag('i');
	packet.addtag('f');
	packet.addi(mNodeID);
	packet.addi(mTriggerID);
	packet.addf(mValue);

	ReplyAddress *users = mWorld->hw->mUsers;
	int numUsers = mWorld->hw->mNumUsers;
	for (int i=0; i<numUsers; ++i) {
		SendReply(users+i, packet.data(), packet.size());
	}
}

void NodeEndMsg::Perform()
{
	scpacket packet;
	switch (mState) {
		case kNode_Go :
			packet.adds("/n_go");
			break;
		case kNode_End :
			packet.adds("/n_end");
			break;
		case kNode_On :
			packet.adds("/n_on");
			break;
		case kNode_Off :
			packet.adds("/n_off");
			break;
		case kNode_Move :
			packet.adds("/n_move");
			break;
	}
	packet.maketags(5);
	packet.addtag(',');
	packet.addtag('i');
	packet.addtag('i');
	packet.addtag('i');
	packet.addtag('i');
	packet.addi(mNodeID);
	packet.addi(mGroupID);
	packet.addi(mPrevNodeID);
	packet.addi(mNextNodeID);

	ReplyAddress *users = mWorld->hw->mUsers;
	int numUsers = mWorld->hw->mNumUsers;
	for (int i=0; i<numUsers; ++i) {
		SendReply(users+i, packet.data(), packet.size());
	}
}

void DeleteGraphDefMsg::Perform()
{
	GraphDef_Free(mDef);
}

void NotifyNoArgs(World *inWorld, char *inString);
void NotifyNoArgs(World *inWorld, char *inString)
{
	scpacket packet;
	packet.adds(inString);

	ReplyAddress *users = inWorld->hw->mUsers;
	int numUsers = inWorld->hw->mNumUsers;
	for (int i=0; i<numUsers; ++i) {
		SendReply(users+i, packet.data(), packet.size());
	}
}


bool SendMsgToEngine(World *inWorld, FifoMsg& inMsg)
{
	return inWorld->hw->mAudioDriver->SendMsgToEngine(inMsg);
}

bool SendMsgFromEngine(World *inWorld, FifoMsg& inMsg)
{
	return inWorld->hw->mAudioDriver->SendMsgFromEngine(inMsg);
}

void SetPrintFunc(PrintFunc func)
{
	gPrint = func;
}


int scprintf(const char *fmt, ...)
{
	va_list vargs;
	va_start(vargs, fmt); 
	
	if (gPrint) return (*gPrint)(fmt, vargs);
	else return vprintf(fmt, vargs);
}
