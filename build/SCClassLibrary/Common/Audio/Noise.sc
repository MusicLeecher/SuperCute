
/* 
	Noise Generators
	
	WhiteNoise.ar(mul, add)
	BrownNoise.ar(mul, add)
	PinkNoise.ar(mul, add)
	Crackle.ar(chaosParam, mul, add)
	LFNoise0.ar(freq, mul, add)
	LFNoise1.ar(freq, mul, add)
	LFNoise2.ar(freq, mul, add)
	Dust.ar(density, mul, add)
	Dust2.ar(density, mul, add)
	
	White, Brown, Pink generators have no modulatable parameters
	other than multiply and add inputs.
	
	The chaos param for ChaosNoise should be from 1.0 to 2.0
	
*/

Rand : UGen {
	// uniform distribution
	*new { arg lo = 0.0, hi = 1.0;
		^this.multiNew('scalar', lo, hi)
	}
}

IRand : UGen {
	// uniform distribution of integers
	*new { arg lo = 0, hi = 127;
		^this.multiNew('scalar', lo, hi)
	}
}

LinRand : UGen {
	// linear distribution
	// if minmax <= 0 then skewed towards lo.
	// else skewed towards hi.
	*new { arg lo = 0.0, hi = 1.0, minmax = 0;
		^this.multiNew('scalar', lo, hi, minmax)
	}
}
NRand : UGen {
	// sum of N uniform distributions.
	// n = 1 : uniform distribution - same as Rand
	// n = 2 : triangular distribution
	// n = 3 : smooth hump
	// as n increases, distribution converges towards gaussian
	*new { arg lo = 0.0, hi = 1.0, n = 0;
		^this.multiNew('scalar', lo, hi, n)
	}
}

ExpRand : UGen {
	// exponential distribution
	*new { arg lo = 0.01, hi = 1.0;
		^this.multiNew('scalar', lo, hi)
	}
}

WhiteNoise : UGen {
	
	*ar { arg mul = 1.0, add = 0.0;
		^this.multiNew('audio').madd(mul, add)
	}
	*kr { arg mul = 1.0, add = 0.0;
		^this.multiNew('control').madd(mul, add)
	}
	
}

BrownNoise : WhiteNoise {
}

PinkNoise : WhiteNoise {
}

PinkerNoise : WhiteNoise {
}

ClipNoise : WhiteNoise {
}

GrayNoise : WhiteNoise {
}


NoahNoise : WhiteNoise {
}

Crackle : UGen {
	
	*ar { arg chaosParam=1.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', chaosParam).madd(mul, add)
	}
	*kr { arg chaosParam=1.5, mul = 1.0, add = 0.0;
		^this.multiNew('control', chaosParam).madd(mul, add)
	}
}

Logistic : UGen {
	
	*ar { arg chaosParam=3.0, freq = 1000.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', chaosParam, freq).madd(mul, add)
	}
	*kr { arg chaosParam=3.0, freq = 1000.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', chaosParam, freq).madd(mul, add)
	}
}

Rossler : UGen {
	
	*ar { arg chaosParam=1.5, dt = 0.04, mul = 1.0, add = 0.0;
		^this.multiNew('audio', chaosParam, dt).madd(mul, add)
	}
	*kr { arg chaosParam=1.5, dt = 0.04, mul = 1.0, add = 0.0;
		^this.multiNew('control', chaosParam, dt).madd(mul, add)
	}
}

LFNoise0 : UGen {
	
	*ar { arg freq=500.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq).madd(mul, add)
	}
	*kr { arg freq=500.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq).madd(mul, add)
	}
}

LFNoise1 : LFNoise0 {
}

LFNoise2 : LFNoise0 {
}

LFClipNoise : LFNoise0 {
}


Dust : UGen {
	
	*ar { arg density = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', density).madd(mul, add)
	}
	*kr { arg density = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', density).madd(mul, add)
	}
	signalRange { ^\unipolar }
	
}

Dust2 : UGen {
	*ar { arg density = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', density).madd(mul, add)
	}
	*kr { arg density = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', density).madd(mul, add)
	}
}

LinCong : UGen {
	var iseed, imul, iadd, imod;
	
	*ar { arg iseed, imul, iadd, imod, mul = 1.0, add = 0.0;
		^this.multiNew('audio', iseed, imul, iadd, imod).madd(mul, add)
	}
	*kr { arg iseed, imul, iadd, imod, mul = 1.0, add = 0.0;
		^this.multiNew('control', iseed, imul, iadd, imod).madd(mul, add)
	}
	init { arg jseed, jmul, jadd, jmod ... theInputs;
 		inputs = theInputs;
 		iseed = jseed;
 		imul = jmul;
 		iadd = jadd;
 		imod = jmod;
 	}
}

Latoocarfian : UGen {
	
	*ar { arg a, b, c, d, mul = 1.0, add = 0.0;
		^this.multiNew('audio', a, b, c, d).madd(mul, add)
	}
	*kr { arg a, b, c, d, mul = 1.0, add = 0.0;
		^this.multiNew('control', a, b, c, d).madd(mul, add)
	}
}
