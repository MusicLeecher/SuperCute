Stethoscope {
	var <server, <numChannels, <rate,  <index;
	var <bufsize, buffer, <window, synth;
	var n, c, d, sl, zx, zy, ai=0, ki=0, audiospec, controlspec;
	

	*new { arg server, numChannels = 2, index, bufsize = 4096, zoom, rate, view;
		if(server.inProcess.not, { "scope works only with internal server".error; ^nil });		^super.newCopyArgs(server, numChannels, rate ? \audio).makeWindow(view)
		.index_(index ? 0).zoom_(zoom).allocBuffer(bufsize).run;
	}
	makeBounds { arg size=212; ^Rect(322, 10, size, size) }
	
	makeWindow { arg view;
		var style=0, sizeToggle=0;
		if(view.isNil) 
		{
			window = SCWindow("stethoscope", this.makeBounds);
			view = window.view;
			view.decorator = FlowLayout(window.view.bounds);
			window.front;
			window.onClose = { this.free };
			
		};
		n = SCScope(view, Rect(0,0, view.bounds.width - 10, view.bounds.height - 40));
		n.background = Color.black;
		n.resize = 5;
		view.keyDownAction = 
			{ arg view, char; 
				if(char === $i) { this.toInputBus }; 
				if(char === $o) { this.toOutputBus }; 
				if(char === $ ) { this.run };
				if(char === $s) { n.style = style = (style + 1) % 2 };
				if(char === $S) { n.style = 2 };
				if(char === $j or: { char.ascii === 0 }) { this.index = index - 1 };
				if(char === $k) { this.switchRate };
				if(char === $l or: { char.ascii === 1 }) { this.index = index + 1 };
				if(char === $-) {  zx = zx + 0.25; n.xZoom = 2 ** zx };
				if(char === $+) {  zx = zx - 0.25; n.xZoom = 2 ** zx };				if(char === $*) {  zy = zy + 0.25; n.yZoom = 2 ** zy };
				if(char === $_) {  zy = zy - 0.25; n.yZoom = 2 ** zy };
				if(char === $A) {  this.adjustBufferSize };
				if(char === $m) { if(sizeToggle == 0) { sizeToggle = 1; this.size_(500) }
												{ sizeToggle = 0; this.size_ }};
				if(char === $.) {  if(synth.isPlaying) { synth.free } };
			};
			
		zx = n.xZoom.log2;
		zy = n.yZoom.log2;
		
		audiospec = ControlSpec(0, server.options.numAudioBusChannels, step:1);
		controlspec = ControlSpec(0, server.options.numControlBusChannels, step:1);
						
		sl = SCSlider(view, Rect(10, 10, view.bounds.width - 100, 20));
		sl.action = { arg x;
				var i; 
				i = this.spec.map(x.value);
				this.index = i;
			};
		sl.resize = 8;
		c = SCNumberBox(view, Rect(10, 10, 30, 20)).value_(0);
		c.action = { this.index = c.value;  };
		c.resize = 9;
		c.font = Font("Monaco", 9);
		d = SCNumberBox(view, Rect(10, 10, 25, 20)).value_(numChannels);
		d.action = { this.numChannels = d.value.asInteger  };
		d.resize = 9;
		d.font = Font("Monaco", 9);
		SCStaticText(view, Rect(10, 10, 20, 20)).visible_(false);
		this.updateColors;
	}
	
	spec { ^if(rate === \audio) { audiospec } {�controlspec } }
	
	setProperties { arg numChannels, index, bufsize=4096, zoom, rate;
				
				if(rate.notNil) { this.rate = rate };
				if(index.notNil) { this.index = index };
				if(numChannels.notNil) {�this.numChannels = numChannels };
				if(this.bufsize != bufsize) { this.allocBuffer(bufsize) };
				if(zoom.notNil) { this.zoom = zoom };
	}
	
	allocBuffer { arg argbufsize;
		bufsize = argbufsize ? bufsize;
		if(buffer.notNil) { buffer.free };
		buffer = Buffer.alloc(server, bufsize, numChannels);
		n.bufnum = buffer.bufnum;
		if(synth.isPlaying) { synth.set(\bufnum, buffer.bufnum) };
	}
	
	run {
		if(synth.isPlaying.not) {
			synth = SynthDef("stethoscope", { arg in, switch, bufnum;
				var z;
				z = Select.ar(switch, [In.ar(in, numChannels), K2A.ar(In.kr(in, numChannels))]); 
				ScopeOut.ar(z, bufnum);
			}).play(server, [\bufnum, buffer.bufnum, \in, index, \switch] 
				++ if('audio' === rate) { 0 } { 1 },
				\addAfter
			);
			synth.isPlaying = true;
			NodeWatcher.register(synth);
		}
	}
	
	free {
		buffer.free;
		if(synth.isPlaying) { synth.free };
		synth = nil;
		if(server.scopeWindow === this) { server.scopeWindow = nil }
	}
	
	numChannels_ { arg n;
	
		var isPlaying;
		if(n != numChannels and: { n > 0 }) {�
			isPlaying = synth.isPlaying;
			if(isPlaying) { synth.free; synth.isPlaying = false; }; // immediate
			numChannels = n;
			
			d.value = n;
			this.allocBuffer;
			if(isPlaying) {�this.run };
			this.updateColors;
		};
	}
	
	index_ { arg val=0;
		var spec;
		spec = this.spec;
		index = spec.constrain(val);
		if(synth.isPlaying) { synth.set(\in, index) };
		if(rate === \audio) { ai = index } { ki = index };
		c.value = index;
		sl.value = spec.unmap(index)
	}
	
	rate_ { arg argRate=\audio;
		
		if(argRate === \audio)
		{ 
				if(synth.isPlaying) { synth.set(\switch, 0) };
				rate = \audio;
				this.updateColors;
				ki = index;
				this.index = ai;
		}
		{
				if(synth.isPlaying) { synth.set(\switch, 1) };
				rate = \control;
				this.updateColors;
				ai = index;
				this.index = ki;
		}
	}
	
	size_ { arg val=212; if(window.notNil) { window.bounds = this.makeBounds(val) } }
	
	zoom_ { arg val;
		val = val ? 1;
		zx = val.log2;
		n.xZoom = val;
	}
	
	updateColors {
		n.waveColors = if(\audio === rate) { 
			Array.fill(numChannels, { rgb(255, 218, 000) }); 
		} { 
			Array.fill(numChannels, { rgb(125, 255, 205) }); 
		}
	}
	
	switchRate { if(rate === \control) { this.rate = \audio } {  this.rate = \control } }
	
	toInputBus {
		this.index = server.options.numOutputBusChannels;
		this.numChannels = server.options.numInputBusChannels;
	}
	toOutputBus {
		this.index = 0;
		this.numChannels = server.options.numOutputBusChannels;
	}
	adjustBufferSize {
		this.allocBuffer(max(256,nextPowerOfTwo(asInteger(n.bounds.width * n.xZoom))))
	}
	

}
