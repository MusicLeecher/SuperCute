
AbstractPlayer : AbstractFunction  { 

	var <path,name,<>dirty=true; 
	
	var <synth,<server,<patchOut,<>readyForPlay = false,defName;
		
	play { arg group,atTime,bus;
		var bundle;
		if(synth.isPlaying,{ ^this });

		if(bus.notNil,{ 
			bus = bus.asBus;
			if(group.isNil,{ 
				server = bus.server;
				group = server.asGroup;
			},{	
				group = group.asGroup;
				server = group.server;
			})
		},{
			group = group.asGroup;
			server = group.server;
			// leave bus nil
		});

		bundle = CXBundle.new;

		if(readyForPlay and: {Library.at(SynthDef,server,this.defName.asSymbol).notNil},{
			this.makePatchOut(group,false,bus,bundle);
			this.spawnToBundle(bundle);
			server.waitForBoot({
				bundle.send(this.server,atTime);
			});
		},{
			Routine({
				var limit = 100,bsize;
				if(server.serverRunning.not,{
					server.startAliveThread(0.1,0.4);
					server.boot(false);
					while({
						server.serverRunning.not 
							and: {(limit = limit - 1) > 0}
					},{
						"waiting for server to boot...".inform;
						0.4.wait;	
					});
					if(server.dumpMode != 0,{ server.stopAliveThread; });
					//atTime is now bogus
				});
				if(server.serverRunning.not,{
					"server failed to start".error;
				},{
					if(server.dumpMode != 0,{
						server.stopAliveThread;
					});
					bsize = this.prepareForPlay(group,false,bus) / 25.0;
					// need some way to track all the preps completion
					// also in some cases the prepare can have a completion
					// tacked on and we might combine with the spawn message
					
					// need a fully fledged OSCMessage that can figure it out
					bsize.wait;
			
					atTime = atTime ? 0;
					if(atTime > bsize,{ atTime = atTime - bsize });
					this.spawnAtTime(atTime);
				});
			}).play(SystemClock);
		});
	}
	prepareForPlay { arg group,private = false,bus;
		var bundle;
		bundle = CXBundle.new;
		group = group.asGroup;
		this.prepareToBundle(group,bundle);
		this.makePatchOut(group,private,bus,bundle);
		^bundle.clumpedSendNow(group.server)
	}
	prepareToBundle { arg group,bundle;
		readyForPlay = false;
		
		group = group.asGroup;
		this.loadDefFileToBundle(bundle,group.server);
		// if made, we have secret controls now
		// else we already had them
		
		// has inputs
		this.children.do({ arg child;
			// loads samples
			child.prepareToBundle(group,bundle);
		});
		
		// not really until the last confirmation comes from server
		readyForPlay = true;
	}

	/* status */
	isPlaying { ^synth.isPlaying }
	stop {
		if(synth.notNil,{
			synth.free;
			synth = nil;
		});
	}
	/*stop {// does not release server  resources
		this.stopMsg.send;
	}
	stopMsg {
		var m;
		if(synth.notNil,{
			m = CXMessage( synth.server, synth.freeMsg );
			//	freeMsg { ^[11, nodeID] }
			synth = nil;
		});
		^m
	}*/
	free {
		if(synth.notNil,{
			synth.free;
			synth = nil;
		});		
		if(patchOut.notNil,{
			patchOut.free;
			patchOut = nil;
		});
		// release any connections i have made
		readyForPlay = false;
	}
	run { arg flag=true;
		if(synth.notNil,{
			synth.run(flag);
		});
	}
	release { arg releaseTime = 0.1,atTime;
		var rb;
		rb = CXBundle.new;
		this.releaseToBundle(releaseTime,rb);
		rb.send(server,atTime);
		if(releaseTime.notNil,{
			AppClock.sched((atTime ? 0) + releaseTime,{
				this.stop;
			})
		})
	}
	releaseToBundle { arg releaseTime,bundle;
		bundle.add(synth.releaseMsg(releaseTime));
	}
		
	busIndex { ^patchOut.index }
	bus { ^patchOut.bus }
	bus_ { arg b;
		b = b.asBus(this.rate,this.numChannels,this.server);
		if(patchOut.notNil,{
			patchOut.bus = b;
			if(synth.isPlaying,{
				synth.set(\out,b.index)
			})
		});
		// otherwise we should have had a patchOut
	}
	group { ^patchOut.group }

	spawn { this.spawnAtTime(nil) }
	spawnAtTime { arg atTime;
		var bundle;
		bundle = CXBundle.new;
		this.spawnToBundle(bundle);
		bundle.send(this.server,atTime);
	}
	spawnToBundle { arg bundle;
		this.children.do({ arg child;
			child.spawnToBundle(bundle);
		});
		synth = Synth.basicNew(this.defName,server);
		("spawning: " + this.defName).debug;
		//patchOut ?? {this.insp("noPatchOut")};
		bundle.add(
			synth.addToTailMsg(this.group,this.synthDefArgs)
		);
		bundle.addMessage(this,\didSpawn);
	}
	spawnOnBus { arg bus,atTime;
		this.spawnOn(bus.server.asGroup,true,bus);
	}
	spawnOn { arg group,bus, atTime;
		var bundle;
		bundle = CXBundle.new;
		this.spawnOnToBundle(group,bus,bundle);
		bundle.send(this.server,atTime);
	}
	spawnOnToBundle { arg group,bus,bundle;
		if(patchOut.isNil,{
			this.makePatchOut(group,true,bus,bundle);
		},{
			if(patchOut.bus != bus,{ patchOut.bus.free });
			patchOut.bus = bus;
			if(patchOut.group != group,{ patchOut.group = group });
		});
		this.spawnToBundle(bundle);
	}
	

	/*
		if defName != classname
			when player saves, save defName and secret args (name -> inputIndex)
				that means you can quickly check, load and execute synthdef
					
		save it all in InstrSynthDef (patch is only one with secret args so far)
	*/
	makePatchOut { arg group,private = false,bus;
		group = group.asGroup;
		server = group.server;
		this.topMakePatchOut(group,private,bus);
		this.childrenMakePatchOut(group,true);
	}
	topMakePatchOut { arg group,private = false,bus;
		if(patchOut.notNil,{
			// does not free any previous bus
			if(bus.notNil and: {patchOut.bus != bus},{
				patchOut.bus = bus;
			});
			if(group.notNil and: {patchOut.group != group},{
				patchOut.group = group;
			});
			if(private,{
				if(patchOut.bus.isAudioOut,{
					patchOut.bus = Bus.audio(group.server,this.numChannels);
				})
			}/*,{
				if(patchOut.bus.isAudioOut.not,{
					patchOut.bus = Bus(\audio,0,this.numChannels,group.server)
				})
			}*/)
		},{
			//Patch doesn't know its numChannels or rate until after it makes the synthDef
			if(this.rate == \audio,{// out yr speakers
				if(private,{
					this.setPatchOut(
						AudioPatchOut(this,group,bus 
								?? {Bus.audio(group.server,this.numChannels)})
						)
				},{			
					this.setPatchOut(
						AudioPatchOut(this,group,bus 
								?? {Bus(\audio,0,this.numChannels,group.server)})
								)
				})
			},{
				if(this.rate == \control,{
					this.setPatchOut(
						ControlPatchOut(this,group,
								bus ?? {Bus.control(group.server,this.numChannels)})
							)
				},{
					(thisMethod.asString + "Wrong output rate: " + this.rate + 
				".  AbstractPlayer cannot prepare this object for play.").die;
				});
			});
		});
		^patchOut
	}
	childrenMakePatchOut { arg group,private = true;
		this.children.do({ arg child;
			child.makePatchOut(group,private,nil)
		});
	}
	setPatchOut { arg po; // not while playing
		patchOut = po;
		if(patchOut.notNil,{
			server = patchOut.server;
		});
	}
	freePatchOut {
		if(patchOut.notNil,{
			patchOut.free;
			patchOut = nil;
		});
		this.children.do({ arg child; 
			if(child.isKindOf(AbstractPlayer),{
				child.freePatchOut 
			});
		})
	}
	
	loadDefFileToBundle { arg bundle,server;
		var def,bytes,dn;

		// can't assume the children are unchanged
		this.children.do({ arg child;
			child.loadDefFileToBundle(bundle,server);
		});

		dn = this.defName;
		if(dn.isNil or: {
			dn = dn.asSymbol;
			// name creation still has a bug, can't depend yet
			if(Library.at(SynthDef,server,dn).notNil,{
				//("already loaded:"+dn).postln;
				false
			},{
				true
			})
		},{
			// save it in the archive of the player
			( "building:" + (this.path ?? {this.name}) ).debug;
			def = this.asSynthDef;
			bytes = def.asBytes;
			bundle.add(["/d_recv", bytes]);
			// even if name was nil before (Patch), its set now
			defName = def.name;
			//("loading def:" + defName).debug;
			// InstrSynthDef watches \serverRunning to clear this
			InstrSynthDef.watchServer(server);
			Library.put(SynthDef,server,defName.asSymbol,true);
		});
	}
	//for now:  always sending, not writing
	writeDefFile {
		this.asSynthDef.writeDefFile;
		this.children.do({ arg child;
			child.writeDefFile;
		});
	}
	
	
	/** SUBCLASSES SHOULD IMPLEMENT **/
	//  this works for simple audio function subclasses
	//  but its probably more complicated if you have inputs
	asSynthDef { 
		^SynthDef(this.defName,{ arg out = 0;
			if(this.rate == \audio,{
				Out.ar(out,this.ar)
			},{
				Out.kr(out,this.kr)
			})
		})
	}
	synthDefArgs { 
		^[\out,patchOut.synthArg]		
	}
	defName {
		^defName ?? {this.class.name.asString}
	}
	didSpawn {	
		if(synth.notNil,{// should always have a synth
			synth.isPlaying = true;
			synth.isRunning = true;
		});
		// if i create groups, set them to isRunning
	}
	rate { ^\audio }
	numChannels { ^1 }
	


	/** hot patching **/
	connectTo { arg hasInput;
		this.connectToPatchIn(hasInput.patchIn,this.isPlaying ? false);
	}
	connectToInputAt { arg player,inputIndex=0;
		this.connectToPatchIn(player.patchIns.at(inputIndex),this.isPlaying ? false)
	}
	connectToPatchIn { arg patchIn,needsValueSetNow = true;
		// if my bus is public, change to private
		if(this.isPlaying and: {this.bus.isAudioOut},{
			//"reallocating bus".debug;
			this.bus = Bus.alloc(this.rate,this.server,this.numChannels);
		});
		this.patchOut.connectTo(patchIn,needsValueSetNow)
	}
	disconnect {
		patchOut.disconnect;
	}
	
	
/*  RECORDING ETC.
	scope 	{ Synth.scope({ Tempo.setTempo; this.ar }) }
	fftScope 	{ Synth.fftScope({ Tempo.setTempo; this.ar }) }
	record	{ arg pathName,headerFormat='SD2',sampleFormat='16 big endian signed';
		 Synth.record({ Tempo.setTempo; this.ar },this.timeDuration, pathName, headerFormat, sampleFormat) 
	}
	write 	{  arg pathName,headerFormat='SD2',sampleFormat='16 big endian signed',duration;
		var dur;
		dur = duration ?? {this.timeDuration};
		if(dur.notNil,{
		 	Synth.write({ Tempo.setTempo; this.ar },dur, pathName, headerFormat, sampleFormat) 
		},{
			(this.name.asString ++ " must have a duration to do Synth.write ").error;
		})
	}
*/	



/* UGEN STYLE USAGE */

	ar {
		^this.subclassResponsibility(thisMethod)
	}
	kr { ^this.ar }
	value {  ^this.ar }
	valueArray { ^this.value }
	inAr {
		// only works immediately in  { }.play
		// for quick experimentation, does not encourage reuse
		// ideally would add itself as a child to the current InstrSynthDef
		this.play;
		^In.ar(this.busIndex,this.numChannels)
	}
	// ugen style syntax
	*ar { arg ... args;
		^this.performList(\new,args).ar
	}
	*kr { arg ... args;
		^this.performList(\new,args).kr
	}

	// function composition
	composeUnaryOp { arg operator;
		^PlayerUnop.new(operator, this)
	}
	composeBinaryOp { arg operator, pattern;
		^PlayerBinop.new(operator, this, pattern)
	}
	reverseComposeBinaryOp { arg operator, pattern;
		^PlayerBinop.new(operator, pattern, this)
	}
	


	// subclasses need only implement beatDuration 
	beatDuration { ^nil } // nil means inf
	timeDuration { var bd;
		bd = this.beatDuration;
		if(bd.notNil,{
			^Tempo.beats2secs(bd)
		},{
			^nil
		});	
	}
	delta { 	^this.beatDuration	}


/*
	// all that is needed to play inside standard patterns
	embedInStream { arg inval;
		// i am one event
		^inval.make({
			var dur;
			// needs to protect against inf / nil !!
			~dur = dur = this.beatDuration ? 8192; // arbitrary long ass time 
			~ugenFunc = { 
				~synth.sched(dur,{ thisSynth.release });
				EnvGen.kr(Env.asr) * this.ar
			}
		}).yield
	}
*/	
	
	// if i am saved/loaded from disk my name is my filename
	// otherwise it is "a MyClassName"
	name { 
		^(name ?? 
		{
			name = if(path.notNil,{ 
						PathName(path).fileName
					},nil);
			name  
		}) 
	}
	asString { ^this.name ?? { super.asString } }

	path_ { arg p; path = PathName(p).asRelativePath }

	// structural utilities
	children { ^#[] }
	deepDo { arg function;// includes self
		function.value(this);
		this.children.do({arg c; 
			var n;
			n = c.tryPerform(\deepDo,function);
			if(n.isNil,{ function.value(c) });
		});
	}	 
	allChildren { 
		var all;
		all = Array.new;
		this.deepDo({ arg child; all = all.add(child) });
		^all
		// includes self
	}
	
	asCompileString { // support arg sensitive formatting
		var stream;
		stream = PrettyPrintStream.on(String(256));
		this.storeOn(stream);
		^stream.contents
	}
	storeParamsOn { arg stream;
		// anything with a path gets stored as abreviated
		var args;
		args = this.storeArgs;
		if(args.notEmpty,{
			stream << "(" <<<* enpath(args) << ")";
		})
	}

	guiClass { ^AbstractPlayerGui }

}


SynthlessPlayer : AbstractPlayer { // should be higher

	var <isPlaying;

	loadDefFileToBundle { }

	spawnToBundle { arg bundle;
		this.children.do({ arg child;
			child.spawnToBundle(bundle);
		});
		bundle.addMessage(this,\didSpawn);
	}
	didSpawn {
		super.didSpawn;
		isPlaying = true;
	}
	free {
		super.free;
		isPlaying = false;
	}
	stop {
		super.stop;
		isPlaying = false;
	}
	releaseToBundle { arg releaseTime = 0.1,bundle;
	}
	connectToPatchIn { arg patchIn,needsValueSetNow = true;
		this.patchOut.connectTo(patchIn,needsValueSetNow)
	}
}

MultiplePlayers : SynthlessPlayer { // SynthlessAggregatePlayer

	// manages multiple players sharing the same bus

	var <>voices;
	
	children { ^this.voices }
	
	rate { ^this.voices.first.rate }
	numChannels { ^this.voices.first.numChannels }
	
	childrenMakePatchOut { arg group,private = false;
		this.voices.do({ arg vo;
			// use mine
			vo.setPatchOut(AudioPatchOut(vo,patchOut.group,patchOut.bus.copy));
			// but your children make their own
			vo.childrenMakePatchOut(group,true);
		})
	}
	setPatchOut { arg po;
		super.setPatchOut(po);
		//everybody plays onto same bus
		this.voices.do({ arg pl;
			// ISSUE if rate is not mine, throw an error
			pl.setPatchOut(po.deepCopy);
		})
	}
	loadDefFileToBundle { arg bundle,server;
		// but not self, has no synthdef
		this.voices.do({ arg pl;
			pl.loadDefFileToBundle(bundle,server)
		})
	}		
	spawnToBundle { arg bundle;
		// but not self, has no synthdef
		this.voices.do({ arg pl;
			pl.spawnToBundle(bundle)
		});
		bundle.addMessage(this,\didSpawn);
	}
//	didSpawn { arg patchIn,synthArgi;
//		super.didSpawn(patchIn,synthArgi);
//		this.voices.do({ arg pl;
//			pl.didSpawn(patchIn,synthArgi);
//		})
//	}
	free {
		this.voices.do({ arg pl;
			pl.free
		});
		super.free;
	}
	stop {
		this.voices.do({ arg pl;
			pl.stop;
		});
		super.stop;
	}
	releaseToBundle { arg releaseTime = 0.1,bundle;
		this.voices.do({ arg pl;
			pl.releaseToBundle(releaseTime,bundle)
		})
	}
}

MultiTrackPlayer : MultiplePlayers { // abstract
	
	// manages multiple players with individual private patchOuts
	// doesn't make a patchOut or synth for itself
	
	topMakePatchOut { arg group;
		server = group.asGroup.server;
		// make a synthless patch out ?
	}
	setPatchOut { arg po;
		//irrelevant for me 
	}
	// as it is for abstractplayer
	childrenMakePatchOut { arg group,private = false;
		this.voices.do({ arg vo;
			vo.makePatchOut(group,true);
		})
	}
}



AbstractPlayerProxy : AbstractPlayer { // won't play if source is nil

	var <>source,<isPlaying = false, <isSleeping = true;

	asSynthDef { ^source.asSynthDef }
	synthDefArgs { ^source.synthDefArgs }
	rate { ^source.rate }
	numChannels { ^source.numChannels }
	defName { ^source.defName }
	spawnToBundle { arg bundle; 
		source.spawnToBundle(bundle);
		bundle.addMessage(this,\didSpawn);
	}
	didSpawn {
		isPlaying = true;
		if(source.notNil, { isSleeping = false });
	}
	instrArgFromControl { arg control;
		^source.instrArgFromControl(control)
	}
	free {
		source.free;
		super.free;
		isPlaying = false;
		isSleeping = true;
	}
	stop {
		isPlaying = false;
		isSleeping = true;
		source.stop;
		super.stop;
	}
	releaseToBundle { arg releaseTime=0.2,bundle;
		source.releaseToBundle(releaseTime,bundle)
	}
	children { ^[source] }
	//called by topMakePatchOut
	setPatchOut { arg po;
		super.setPatchOut(po);
		// a copy to the source
		if(source.notNil,{
			source.setPatchOut(PatchOut(source,patchOut.group,patchOut.bus.copy));
		});
	}
	childrenMakePatchOut { arg group,private;
		source.childrenMakePatchOut(group,private);
	}
	
}

