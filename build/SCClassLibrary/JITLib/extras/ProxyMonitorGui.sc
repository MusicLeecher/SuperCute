
	
		var skin = GUI.skin;
		
				if(proxy.notNil) { proxy.vol_(ampSpec.map(slid.value)); } 
			});
			;
			// swingosc: 16 is normal  24 is alt, ctl is off, and fn is 16 as well
				.font_(font)
					["-<", skin.fontColor, skin.onColor]
				.action_({ |box, mod|
					box.value_(1 - box.value)
			.font_(font)
			.action_({ arg btn; 
			.font_(font)
				mod.postln;
					// alt-click osx, swingosc
				btn.value_(1 - btn.value)
			;
		 
		var currVol=0, pxname="", plays=0, playsSpread=false, pauses=0, canSend=0; 
		
		
		
			canSend = proxy.objects.notEmpty.binaryValue;
			
			plays = monitor.isPlaying.binaryValue;
			
			if (monitor.notNil, { 
				playsSpread = proxy.monitor.hasSeriesOuts.not;
		};
		
		currState = [currVol, pxname, plays, outs, playsSpread, pauses, canSend];
		
		if (currState != oldState) { 
		//	"updating".postln; 
		
			ampSl.value_(currVol);
			nameBut.object_(pxname);
			pauseBut.value_(pauses);
			playBut.value_(plays);
			sendBut.value_(canSend);
			
			if (setOutBox.hasFocus.not) {	 
				setOutBox.value_(try { outs[0] } ? 0);
				if (usesPlayN) { 
					playNDialogBut.value_(playsSpread.binaryValue)
				};
			}
		};
		oldState = currState;