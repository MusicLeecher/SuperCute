/*
	Osc - oscillator
	arguments :
		bufnum - an index to a buffer
		freq - frequency in cycles per second
		pm - phase modulation 
		mul - multiply by signal or scalar
		add - add to signal or scalar
*/

Osc : UGen {	
	*ar { 
		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('audio', bufnum, freq, phase).madd(mul, add)
	}
	*kr {
		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('control', bufnum, freq, phase).madd(mul, add)
	}
}

SinOsc : UGen {	
	*ar { 
		arg freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('audio', freq, phase).madd(mul, add)
	}
	*kr {
		arg freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('control', freq, phase).madd(mul, add)
	}
}

OscN : UGen {	
	*ar { 
		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('audio', bufnum, freq, phase).madd(mul, add)
	}
	*kr {
		arg bufnum, freq=440.0, phase=0.0, mul=1.0, add=0.0;
		^this.multiNew('control', bufnum, freq, phase).madd(mul, add)
	}
}

COsc : UGen {	
	*ar { 
		arg bufnum, freq=440.0, beats=0.5, mul=1.0, add=0.0;
		^this.multiNew('audio', bufnum, freq, beats).madd(mul, add)
	}
	*kr {
		arg bufnum, freq=440.0, beats=0.5, mul=1.0, add=0.0;
		^this.multiNew('control', bufnum, freq, beats).madd(mul, add)
	}
}

Formant : UGen {
	*ar {
		arg fundfreq = 440.0, formfreq = 1760.0, bwfreq = 880.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', fundfreq, formfreq, bwfreq).madd(mul, add)
	}
}

LFSaw : UGen {
	*ar {
		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, iphase).madd(mul, add)
	}
	*kr {
		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, iphase).madd(mul, add)
	}
}

LFTri : LFSaw
{
}


LFPulse : UGen {
	*ar {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, iphase, width).madd(mul, add)
	}
	*kr {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, iphase, width).madd(mul, add)
	}
	signalRange { ^\unipolar }
}

VarSaw : UGen {
	*ar {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, iphase, width).madd(mul, add)
	}
	*kr {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, iphase, width).madd(mul, add)
	}
}

Impulse : UGen {
	*ar {
		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, iphase).madd(mul, add)
	}
	*kr {
		arg freq = 440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, iphase).madd(mul, add)
	}
	signalRange { ^\unipolar }
}


SyncSaw : UGen {
	*ar {
		arg syncFreq = 440.0, sawFreq = 440.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', syncFreq, sawFreq).madd(mul, add)
	}
	*kr {
		arg syncFreq = 440.0, sawFreq = 440.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', syncFreq, sawFreq).madd(mul, add)
	}
}


TPulse : UGen {//exception in GrafDef_Load: UGen 'TPulse' not installed.
	*ar {
		arg trig = 0.0, freq = 440.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', trig, freq, width).madd(mul, add)
	}
	*kr {
		arg trig = 0.0, freq = 440.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('control', trig, freq, width).madd(mul, add)
	}
	signalRange { ^\unipolar }
}

Index : UGen {
	*ar {
		arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', bufnum, in).madd(mul, add)
	}
	*kr {
		arg bufnum, in = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', bufnum, in).madd(mul, add)
	}
}

WrapIndex : Index {
}

Shaper : Index {
}


DegreeToKey : UGen {
	*ar {
		arg bufnum, in = 0.0, octave = 12.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', bufnum, in, octave).madd(mul, add)
	}
	*kr {
		arg bufnum, in = 0.0, octave = 12.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', bufnum, in, octave).madd(mul, add)
	}
}

Select : UGen {
	*ar {
		arg which, array;
		^this.multiNewList(['audio', which] ++ array)
	}
	*kr {
		arg which, array;
		^this.multiNewList(['control', which] ++ array)
	}
}

