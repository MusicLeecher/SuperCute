GraphBuilder {
	//used to create an out ugen automatically and a fade envelope
	
	*wrapOut { arg name, func, lags, prependArgs, outClass=\Out, fadeTime;
		^SynthDef.new(name, { arg i_out=0;
			var result, rate, env;
			result = SynthDef.wrap(func, lags, prependArgs);
			rate = result.rate;
			
			if(rate === \scalar,{
				// Out, SendTrig etc. probably a 0.0
				result
			},{
				env = if(fadeTime.notNil, { this.makeFadeEnv(fadeTime) }, 1);
				result = result * env;
				outClass.asClass.multiNewList([rate, i_out]++result)
			})
		})
	}

	*makeFadeEnv { arg fadeTime;
		var dt, gate;
		#dt, gate = Control.names(['_fadeTime', '_gate']).kr([fadeTime, 1.0]);
		^Linen.kr(gate, dt, 1.0, dt, 2);
	
	}




}