+ Pattern {

	asScoreStreamPlayer{|protoEvent|
		^ScoreStreamPlayer(this.asStream, protoEvent);

	}
	
	asScore{|numberOfEvents=100, protoEvent|

	^this.asScoreStreamPlayer(protoEvent).read(numberOfEvents)
	}
}

//+ Stream {
//	asScore { arg maxTime=60;
//		^RenderNotePlayer(maxTime).read(this).score
//			
//	}
//}
+ Object {
	
	render { arg path, maxTime=60, sampleRate = 44100, 
			headerFormat = "AIFF", sampleFormat = "int16", options, inputFilePath;
			
		var file, oscFilePath, score;
		oscFilePath = "temp_oscscore" ++ UniqueID.next;
		score = this.asScore(maxTime);
		score.recordNRT(
			oscFilePath, path, inputFilePath, sampleRate = 44100, headerFormat, sampleFormat, 
			options, "; rm" + oscFilePath
		);
	}

}
