

+ Object {
	
	rate { ^\scalar }
	
	//play { ^ScalarPatchOut(this) }

	makePatchOut {}
	patchOut { ^ScalarPatchOut(this) }
	isPlaying { ^false }

	prepareToBundle {  }
	prepareForPlay {	arg group;
		var bundle;
		bundle = CXBundle.new;
		group = group.asGroup;
		this.prepareToBundle(group,bundle);
		^bundle.clumpedSendNow(group.server)
	}
	spawnToBundle {}
	loadDefFileToBundle {}
	//writeDefFile {}
	
	stop {}
	free {}
	//didSpawn {}

	addToSynthDef {  arg synthDef,name;
		synthDef.addInstrOnlyArg(name,this.synthArg); // has to be an InstrSynthDef
	}
	
	synthArg { ^this }
	instrArgRate { ^\scalar }
	initForSynthDef {}
	instrArgFromControl { arg control;
		^this
	}
	
}

+ Array {
	rate { ^this.at(0).rate } // no attempt to error check you
}


+ TempoBus {
	addToSynthDef {  arg synthDef,name;
		synthDef.addIr(name,this.synthArg);
	}
	synthArg { ^this.index }
	instrArgRate { ^\control }
	instrArgFromControl { arg control;
		^control
	}
	//didSpawn  set value
}

+ AbstractPlayer {

	addToSynthDef {  arg synthDef,name;
		// value doesn't matter so much, we are going to pass in a real live one
		synthDef.addKr(name,this.synthArg ? 0); // \out is a .kr bus index
	}

	synthArg { ^patchOut.synthArg }
	instrArgRate { ^\audio }
	instrArgFromControl { arg control;
		// a Patch could be either
		^if(this.rate == \audio,{
			In.ar(control,this.numChannels)
		},{
			In.kr(control,this.numChannels)
		})
	}
}

+ KrPlayer {

	instrArgRate { ^\control }
	instrArgFromControl { arg control;
		^In.kr(control,this.numChannels)
	}
}

+ Buffer {
	synthArg {
		^bufnum
	}
}



// trig things, supply an InTrig.kr

