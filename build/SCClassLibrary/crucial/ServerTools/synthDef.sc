

+ Object {
	
	stopToBundle {}
	freeToBundle {}
	
	rate { ^\scalar }
	
	makePatchOut {}
	patchOut { ^ScalarPatchOut(this) }
	connectToPatchIn {}

	prepareToBundle {  }
	prepareForPlay {	arg group,private,bus;
		var bundle;
		bundle = CXBundle.new;
		group = group.asGroup;
		this.prepareToBundle(group,bundle);
		^bundle.clumpedSendNow(group.server)
	}
	spawnToBundle {}
	spawnOnToBundle {}
	loadDefFileToBundle {}
	
	addToSynthDef {  arg instrSynthDef,name;
		instrSynthDef.addInstrOnlyArg(name,this.synthArg);
	}
	
	synthArg { ^this }
	initForSynthDef {}
	instrArgFromControl { arg control;
		^this
	}	
}

+ Node {
	freeToBundle { arg bundle;
		bundle.add(this.freeMsg)
	}
}
		
+ Buffer {
	synthArg {
		^bufnum
	}
}

+ SynthDef {
	prepareToBundle { arg group,bundle;
		bundle.add(["/d_recv", this.asBytes]);
	}
}

+ Pattern {
	rate { ^\stream }
	addToSynthDef {  arg synthDef,name;
		synthDef.addIr(name,this.synthArg);
	}
	// no idea until the pattern starts, so
	// give some answer to build the synth def with
	synthArg { ^1 }
	instrArgFromControl { arg control; ^1 }
}
+ Stream {
	rate { ^\stream }
	addToSynthDef {  arg synthDef,name;
		synthDef.addIr(name,this.synthArg);
	}
	// no idea until the pattern starts, so
	// give some answer to build the synth def with
	synthArg { ^1 }
	instrArgFromControl { arg control; ^1 }
}
+ Function { // treat it like a stream or transform into a Patch ?
	rate { ^\stream }
}

+ Spec {
	canAccept { ^false } // canAcceptDrag or other uses
}

+ ControlSpec {
	canAccept { arg thing;
		var ts;
		^if(thing.isNumber,{
			thing.inclusivelyBetween(clipLo,clipHi)
		},{
			if(thing.isKindOf(AbstractPlayer),{
				(thing.spec.class == this.class and: {
					ts = thing.spec;
					// thing does not exceed my bounds
					//ts.clipHi <= clipHi and: {ts.clipLo >= clipLo}
					// need more detail
					true
				})
			},{
				false
			})
		});
	}
}


