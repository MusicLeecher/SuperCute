/*
	PlayBuf - sample player
*/

PlayBuf : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, rate=1.0, trigger=1.0, startPos=0.0, loop = 0.0;
		^this.multiNew('audio', numChannels, bufnum, rate, trigger, startPos, loop)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
}


/*
// exception in GrafDef_Load: UGen 'SimpleLoopBuf' not installed.

SimpleLoopBuf : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, loopStart=0.0, loopEnd=99999.0, trigger=0.0;
		^this.multiNew('audio', numChannels, bufnum, loopStart, loopEnd, trigger)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
}
*/

BufRd : MultiOutUGen {	
	*ar { arg numChannels, bufnum=0, phase=0.0, loop=1.0, interpolation=2;
		^this.multiNew('audio', numChannels, bufnum, phase, loop, interpolation)
	}
	
	init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }
}

BufWr : UGen {	
	*ar { arg inputArray, bufnum=0, phase=0.0, loop=1.0;
		this.multiNewList(['audio', bufnum, phase, 
			loop] ++ inputArray.asArray)
		^inputArray
	}
}


RecordBuf : UGen {	
	*ar { arg inputArray, bufnum=0, offset=0.0, recLevel=1.0, preLevel=0.0, run=1.0, loop=1.0, trigger=1.0;
		this.multiNewList(['audio', bufnum, offset, recLevel, preLevel, run, loop, trigger ] ++ inputArray.asArray);
		^inputArray
	}
}


ScopeOut : UGen {
	*ar { arg bufnum=0, in=0.0, zoom=1.0;
		this.multiNew('audio', bufnum, in, zoom);
		^0.0
	}
	*kr { arg bufnum=0, in=0.0, zoom=1.0;
		this.multiNew('control', bufnum, in, zoom);
		^0.0
	}
}

Tap : UGen {
	*ar { arg bufnum = 0, numChannels = 1, delaytime = 0.2;
		var n;
		n = delaytime * SampleRate.ir.neg; // this depends on the session sample rate, not buffer.
		^PlayBuf.ar(numChannels, bufnum, 1, 0, n, 1);
	}
}


