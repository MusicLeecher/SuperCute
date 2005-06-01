
//these extensions provide means to wrap Objects so that they 
//make sense within the server bus system according to a node proxy.

////////////////////// graphs, functions, numericals and arrays //////////////////

+Object { 
	
	//objects can define their own wrapper classes dependant on 
	//how they play, stop, build, etc. see SynthDefControl for example
	//the original (wrapped) object can be reached by the .source message
	//for objects that only create ugen graphs, define prepareForProxySynthDef(proxy)
	
	proxyControlClass {
		^SynthDefControl
	}
	
	makeProxyControl { arg channelOffset=0;
		^this.proxyControlClass.new(this, channelOffset); 
	}
	
	
	//any preparations that have to be done to prepare the object
	//implement 'prepareForProxySynthDef' to return a ugen func
	
	
	//this method is called from within the Control
	buildForProxy { arg proxy, channelOffset=0, index;
		var argNames;
		argNames = this.argNames;
		^ProxySynthDef(
			ProxySynthDef.tempPrefix ++ proxy.generateUniqueName ++ index,
			this.prepareForProxySynthDef(proxy),
			proxy.nodeMap.ratesFor(argNames),
			nil, 
			true, 
			channelOffset,
			proxy.numChannels
		); 
	}
	prepareForProxySynthDef { ^this.subclassResponsibility(thisMethod) }
	clear { "Object-clear will be removed..".warn; ^this }
	
	defaultArgs { ^nil }
	argNames { ^nil }
	
	//support for unop / binop proxy
	isNeutral { ^true }
	initBus { ^true }
	wakeUp {}
	
}

+Function {
	prepareForProxySynthDef { ^this }
	argNames { ^def.argNames }
	defaultArgs { ^def.prototypeFrame }
}


+SimpleNumber {
	
	prepareForProxySynthDef { arg proxy;
		proxy.initBus(\control, 1);
		^if(proxy.rate === 'audio') {
			{K2A.ar(Control.ir(this))} 
		} { 
			{Control.ir(this)} 
		}
	}
}


+RawArray {
	prepareForProxySynthDef { arg proxy;
		proxy.initBus(\control, this.size);
		^if(proxy.rate === 'audio') {
			{K2A.ar(Control.ir(this))}
		} { 
			{Control.ir(this)}
		}
	}
}

+SequenceableCollection {
	prepareForProxySynthDef { arg proxy;
		proxy.initBus(\control, this.size);
		^{ this.collect({ |el| el.prepareForProxySynthDef(proxy).value }) }
	}
}

+BusPlug {
	prepareForProxySynthDef { arg proxy;
		proxy.initBus(this.rate, this.numChannels);
		^{ this.value(proxy) }
	}

}

+AbstractOpPlug {
	prepareForProxySynthDef { arg proxy;
		proxy.initBus(this.rate, this.numChannels);
		^{ this.value(proxy) }
	}
}

//needs a visit: lazy init + channelOffset

+Bus {
	prepareForProxySynthDef { arg proxy;
		^BusPlug.for(this).prepareForProxySynthDef(proxy);
	}
}




///////////////////////////// SynthDefs and alike ////////////////////////////////////


+SynthDef {
	buildForProxy {}
	numChannels { ^nil } //don't know
	rate { ^nil }
}



+Symbol {
	buildForProxy {}
	proxyControlClass {
		^SynthControl
	}
}

///////////////////////////// Pattern - Streams ///////////////////////////////////


+Stream {
	proxyControlClass { ^StreamControl }
	buildForProxy { ^PauseStream.new(this) }
}

+PauseStream {
	buildForProxy { ^this }
	proxyControlClass { ^StreamControl }
}

+PatternProxy {
	buildForProxy {  arg proxy, channelOffset=0; 
		^this.endless.buildForProxy(proxy, channelOffset) 
	}
}


+Pattern {
	proxyControlClass { ^PatternControl }
	
	buildForProxy { arg proxy, channelOffset=0;
		var player = this.asEventStreamPlayer;
		var event = player.event.buildForProxy(proxy, channelOffset);
		^event !? { player };
	}
}

+ Event {
	//proxyControlClass { ^StreamControl } // does not yet work as input
	buildForProxy { arg proxy, channelOffset=0;
		var ok, index, server, numChannels, rate;
		ok = if(proxy.isNeutral) { 
			rate = this.at(\rate) ? 'audio';
			numChannels = this.at(\numChannels) ? NodeProxy.defaultNumAudio;
			proxy.initBus(rate, numChannels);
		} {
			rate = proxy.rate; // if proxy is initialized, it is user's responsibility
			numChannels = proxy.numChannels;
			true
		};
		^if(ok) { 
				index = proxy.index;
				server = proxy.server;
				this.use({
					~channelOffset = channelOffset; // default value
					~out = { ~channelOffset % numChannels + index };
					~server = server; // not safe for server changes yet
					~finish = {
						~out = ~out.value;
						~group = proxy.group.asNodeID;// group shouldn't be assigned by the user
					}
				});
		} { nil }
	}
}



/////////// pluggable associations //////////////


+Association {
	buildForProxy { arg proxy, channelOffset=0, index;
		^AbstractPlayControl.buildMethods[key].value(value, proxy, channelOffset, index)
	}
	proxyControlClass {
		^AbstractPlayControl.proxyControlClasses[key] ? SynthDefControl
	}
}

+AbstractPlayControl {
	makeProxyControl { ^this.deepCopy } //already wrapped, but needs to be copied
	
	/* these adverbial extendible interfaces are for supporting different role schemes.
	it is called by Association, so ~out = \filter -> ... will call this. The first arg passed is 	the value of the association */

	*initClass {
		proxyControlClasses = (
			filter: SynthDefControl, 
			set: StreamControl
		);
		
		buildMethods = ( 		
		
		filter: #{ arg func, proxy, channelOffset=0, index;
			var ok, ugen;
			if(proxy.isNeutral) { 
				ugen = func.value(Silent.ar);
				ok = proxy.initBus(ugen.rate, ugen.numChannels);
				if(ok.not) { Error("wrong rate/numChannels").throw }
			};
			
			{ arg out;
				var e;
				e = EnvGate.new * Control.names(["wet"++(index ? 0)]).kr(1.0);
				if(proxy.rate === 'audio') {
					XOut.ar(out, e, SynthDef.wrap(func, nil, [In.ar(out, proxy.numChannels)]))
				} {
					XOut.kr(out, e, SynthDef.wrap(func, nil, [In.kr(out, proxy.numChannels)]))				};
			}.buildForProxy( proxy, channelOffset, index )
		
		},
		set: #{ arg pattern, proxy, channelOffset=0, index;
			var args;
			args = proxy.controlNames.collect(_.name);
			args = args.removeAll(#[\out, \i_out, \gate, \fadeTime]);
			Pbindf(
				pattern,
				\type, \set,
				\id, Pfunc { proxy.group.nodeID },
				\args, args
			).buildForProxy( proxy, channelOffset, index )
		}
		
		)
	
	}
}



