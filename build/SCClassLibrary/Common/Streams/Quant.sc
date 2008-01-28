// This class is used to encapsulate quantization issues associated with EventStreamPlayer and TempoClock
// quant and phase determine the starting time of something scheduled by a TempoClock
// offset is an additional timing factor that allows an EventStream to compute "ahead of time" enough to allow
// negative lags for strumming a chord, etc
Quant {
	classvar	default;
	var <>quant, <>phase, <>offset;

	*default { ^default ?? { Quant.new } }
	*default_ { |quant| default = quant.asQuant }

	*new { |quant = 0, phase, offset| ^super.newCopyArgs(quant, phase, offset) }
	
	nextTimeOnGrid { | clock |
		^clock.nextTimeOnGrid(quant, (phase ? 0) - (offset ? 0));
	}

	asQuant { ^this.copy }
}

