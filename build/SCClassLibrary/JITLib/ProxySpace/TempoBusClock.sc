

TempoBusClock : TempoClock {
	var <>control;
	classvar <>default;
	
	*new { arg control, tempo, beats, seconds;
		^super.new(tempo, beats, seconds).control_(control)
	}
	
	setTempoAtBeat { arg newTempo, beats;
		control.set(0, newTempo, \fadeTime, 0.0);
		^super.setTempoAtBeat(newTempo, beats)
	}
	
	setTempoAtSec { arg newTempo, secs;
		control.set(0, newTempo, \fadeTime, 0.0)
		^super.setTempoAtSec(newTempo, secs)
	}

}

