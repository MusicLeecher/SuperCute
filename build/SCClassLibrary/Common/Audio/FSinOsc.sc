/*
	FSinOsc - fixed frequency sine oscillator
	arguments :
		freq - frequency in cycles per second. Must be a scalar.
		mul - multiply by signal or scalar
		add - add to signal or scalar
		
	This unit generator uses a very fast algorithm for generating a sine
	wave at a fixed frequency.
*/

FSinOsc : UGen {	
	*ar { arg freq=440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, iphase).madd(mul, add)
	}
	*kr { arg freq=440.0, iphase = 0.0, mul = 1.0, add = 0.0;
		^this.multiNew('control', freq, iphase).madd(mul, add)
	}
}


Klang : UGen {
	*ar { arg specificationsArrayRef, freqscale = 1.0, freqoffset = 0.0;
		var specs, freqs, amps, phases;
		# freqs, amps, phases = specificationsArrayRef.value;
		specs = [freqs, 
				amps ?? {Array.fill(freqs.size,1.0)}, 
				phases ?? {Array.fill(freqs.size,0.0)}
				].flop.flat;
		^this.multiNewList(['audio', freqscale, freqoffset] ++ specs )
	}
}

Klank : UGen {	
	*ar { arg specificationsArrayRef, input, freqscale = 1.0, freqoffset = 0.0, decayscale = 1.0;
		var specs, freqs, amps, times;
		# freqs, amps, times = specificationsArrayRef.value;
		specs = [freqs, 
				amps ?? {Array.fill(freqs.size,1.0)}, 
				times ?? {Array.fill(freqs.size,1.0)}
				].flop.flat;
		^this.multiNewList(['audio', input, freqscale, freqoffset, decayscale] ++ specs )
	}
}


Blip : UGen {	
	*ar { arg freq=440.0, numharm = 200.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, numharm).madd(mul, add)
	}
}

Saw : UGen {	
	*ar { arg freq=440.0, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq).madd(mul, add)
	}
}

Pulse : UGen {	
	*ar { arg freq=440.0, width = 0.5, mul = 1.0, add = 0.0;
		^this.multiNew('audio', freq, width).madd(mul, add)
	}
}
