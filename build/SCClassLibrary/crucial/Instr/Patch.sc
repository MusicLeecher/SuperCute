
HasPatchIns : AbstractPlayer {

	var <patchIns;

	didSpawn {
		super.didSpawn;
		//i know of the synth, i hand out the NodeControls
		patchIns.do({ arg patchIn,argi;
			patchIn.nodeControl_(NodeControl(synth,this.synthArgsIndices.at(argi)));
			this.inputs.at(argi).connectToPatchIn(patchIn,false);
		});
	}
	//subclassResponsibility
	synthArgsIndices { ^this.subclassResponsibility(thisMethod) }
	inputs { ^this.subclassResponsibility(thisMethod) }
	// specAt
	// argNameAt

	mapInputToBus { arg i,bus;
		var patchOut;
		bus = bus.asBus;
		patchOut = PatchOut.performList(bus.rate,[nil,bus.server.asGroup,bus]);
		patchOut.connectTo(patchIns.at(i), this.isPlaying );
	}
	
/*
have to bundle it
	connectInputToPlayer { arg i,player;
		// does it have patchOut
		if(player.patchOut.isNil,{
			// always uncomfortable to not have patchOut decided
			player.makePatchOut(this.group,true);
		});
		player.patchOut.connectTo(patchIns.at(i), this.isPlaying);
	}
*/				
	/*
	setInput { arg i,newarg;
		var old,newargpatchOut;
		old = args.at(i);
		args.put(i,newarg);
		if(this.isPlaying,{
			old.free; // release old  thru some manager ?
			newarg
			
			//old.patchOut.releaseConnection;
			newargpatchOut = newarg.play(Destination.newByRate(this.instr.specs.at(i).rate,
								NodeControl(patchOut.synth,i + 1)));
			newargpatchOut.retainConnection;
			newargpatchOut.connectTo(patchIns.at(i));
		});
	}
	*/
	

	inputProxies { // just this patch
		^this.inputs.select({ arg a; a.isKindOf(PlayerInputProxy) })
	}
	// deep search
	annotatedInputProxies { arg offset=0,array; // [ input , deepOffset, argName, spec ]
		var inputs;
		inputs = this.inputs;
		if(array.isNil,{ array = [] });
		inputs.do({ arg a,i;
			if(a.isKindOf(PlayerInputProxy),{
				array = array.add([a, offset + i, this.argNameAt(i), this.specAt(i) ]);
			},{
				if(a.isKindOf(HasPatchIns),{
					a.annotatedInputProxies(offset + i, array)
				})
			})
		});
		^array
	}
	
	controllables { arg offset=0, array;
		var inputs;
		var defaultControl;
		inputs = this.inputs;
		if(array.isNil,{ array = [] });
		inputs.do({ arg a,i;
			var spec;
			spec = this.specAt(i);
			if(spec.rate == \control /* and: {
				a.isNumber or: {a.isKindOf(KrNumberEditor)} or: {
					defaultControl = ControlPrototypes.forSpec(spec,this.argNameAt(i));
					[a,defaultControl].insp;
					a.isKindOf(defaultControl.class)
				}
			}*/,{ // if
				array = array.add([a, offset + i, this.argNameAt(i), this.specAt(i) ]);
			},{ // else
				if(a.isKindOf(HasPatchIns),{
					a.controllables(offset + i, array)
				})
			})
		});
		^array
	}
	setInput { arg ai,ag;
		^this.subclassResponsibility(thisMethod)
	}
	setDeepInput { arg ai,ag,offset=0;
		var inputs;
		inputs = this.inputs;
		if(inputs.size + offset > ai,{
			this.setInput(ai - offset, ag);
			^true
		});
		offset = offset + inputs.size;
		^inputs.any({ arg a,i;
			var set=false;
			a.isKindOf(HasPatchIns) and: { 
				set = a.setDeepInput(ai,ag,offset + i);
				offset = offset + a.inputs.size;
				set
			}
		})
	}
	deepSpecAt { arg ai,offset=0;
		var inputs,deepSpec;
		inputs = this.inputs;
		if(inputs.size + offset > ai,{
			^this.specAt(ai - offset)
		});
		offset = offset + inputs.size;
		inputs.detect({ arg a,i;
			a.isKindOf(HasPatchIns) and: { 
				deepSpec = a.deepSpecAt(ai,offset + i);
				offset = offset + a.inputs.size;
				deepSpec.notNil
			}
		});
		^deepSpec
	}		
}

Patch : HasPatchIns  {
		
	var <instr,<>args;
	var synthPatchIns,<argsForSynth,<synthArgsIndices;
	
	var synthDef;
	var <numChannels,<rate; // determined after making synthdef
	
	*new { arg name,inputs;
		^super.new.loadSubject(name).createArgs(loadDocument(inputs) ? [])
	}
	storeParamsOn { arg stream;
		var last;
		if(this.class === Patch,{ // an indulgence ...
			last = args.size - 1;
			// anything with a path gets stored as abreviated
			stream << "(" <<< this.instr.name << ",[";
				if(stream.isKindOf(PrettyPrintStream),{ stream.indent(1); });
				args.do({ arg ag,i;
					stream.nl;
					stream <<< enpath(ag);
					if(i != last,{ stream << "," });
				});
				if(stream.isKindOf(PrettyPrintStream),{ stream.indent(-1); });
			stream.nl;
			stream << "])";
		},{
			super.storeParamsOn(stream)
		});
	}
	storeArgs { ^[this.instr.name,args] }

	inputs { ^args }
	setInput { arg ai, ag;
		var synthArgi;
		args.put(ai,ag);
		synthArgi = synthArgsIndices.at(ai);
		if(synthArgi.notNil,{
			argsForSynth.put(synthArgi,ag);
		});
	}
	argNameAt { arg i; ^instr.argNameAt(i) }
	specAt { arg i; ^instr.specs.at(i) }
	indexFromName { arg argName;
		^instr.argNames.indexOf(argName)
	}
	argFromName { arg argName;
		var index;
		index = this.indexFromName(argName);
		if(index.notNil,{
			^args.at(index)
		},{
			^nil
		})
	}
		
	loadSubject { arg name;
		instr = name.asInstr;
		if(instr.isNil,{
			("Instrument not found !!" + name).die;
		});
	}

	createArgs { arg argargs;
		var argsSize;
		argsForSynth = [];
		patchIns = [];
		synthPatchIns = [];
		argsSize = this.instr.argsSize;
		synthArgsIndices = Array.newClear(argsSize);

		args=Array.fill(argsSize,{arg i; 
			var proto,spec,ag,patchIn,darg;
			spec = instr.specs.at(i);
			ag = 
				argargs.at(i) // explictly specified
				?? 
				{ //  or auto-create a suitable control...
					proto = spec.defaultControl;
					darg = instr.initAt(i);
					if(darg.isNumber,{
						proto.tryPerform('value_',darg);
					});
					proto
				};
				
			patchIn = PatchIn.newByRate(spec.rate);
			patchIns = patchIns.add(patchIn);

			// although input is control, arg could overide that
			if(spec.rate != \scalar
				and: {ag.rate != \scalar}
			,{
				argsForSynth = argsForSynth.add(ag);
				synthPatchIns = synthPatchIns.add(patchIn);
				synthArgsIndices.put(i,synthPatchIns.size - 1);
			});
			
			ag		
		});
	}
	
	asSynthDef {
		// could be cached, must be able to invalidate it
		// if an input changes
		^synthDef ?? {
			synthDef = InstrSynthDef.build(this.instr,this.args,Out);
			defName = synthDef.name;
			numChannels = synthDef.numChannels;
			rate = synthDef.rate;
			synthDef
		}
	}
	invalidateSynthDef { synthDef = nil; }
	
	// has inputs
	spawnToBundle { arg bundle;
		var synthArgs;
		if(patchOut.isNil,{ 
			"Patch-spawnToBundle : this Patch has not yet been prepared".die(this);
		});
		this.asSynthDef;// make sure it exists
		
		this.children.do({ arg child;
			child.spawnToBundle(bundle);
		});
		synth = Synth.basicNew(this.defName,this.server);
		NodeWatcher.register(synth);
		bundle.add(
			synth.addToTailMsg(patchOut.group,
				this.synthDefArgs
				++ synthDef.secretDefArgs(args)
			)
		);
		bundle.addAction(this,\didSpawn);
	}
	synthDefArgs {
		// not every arg makes it into the synth def
		var args;
		args = Array(argsForSynth.size * 2 + 2);
		argsForSynth.do({ arg ag,i;
			args.add(i);
			args.add(ag.synthArg);
		});
		args.add(\out);
		args.add(patchOut.synthArg);
		^args
	}
	defName { ^defName } // NOT 'Patch' ever
	
	free {
		super.free;
		// ISSUE: if you change a static non-synth input 
		// nobody notices to rebuild the synth def
		// so for now, wipe it out
		// the Instr knows if it came from a file, can check the moddate
		synthDef = nil;
	}
	
	// act like a simple ugen function
	// mostly this won't work except with simple UGens
	ar { arg ... overideArgs;	^this.valueArray(overideArgs) }
	value { arg ... overideArgs;  ^this.valueArray(overideArgs) }
	valueArray { arg  overideArgs;  
		// each arg is valued as it is passed into the instr function
		^instr.valueArray(
				args.collect({ arg a,i; (overideArgs.at(i) ? a).value; })  
			)
	}
	printOn { arg s; s << this.class.name << instr.name; }
	
	/*storeModifiersOn { arg stream;
		// this allows a known defName to be used to look up in the cache
		// otherwise a Patch doesn't know its defName until after building
		if(defName.notNil,{
			stream << ".defName_(" <<< defName << ")";
		})
	}*/
	/*defName_ { arg df;
		// for reloading from storeModifiersOn
		defName = df;
	}*/
	
	children { ^args }
	guiClass { ^PatchGui }
}


/*
EfxPatch : Patch

	one meant for efx should be a specific type
	then it can share its bus with the primary input	
*/

