
ServerOptions
{
	var <>numAudioBusChannels=128;
	var <>numControlBusChannels=4096;
	var <>numInputBusChannels=2;
	var <>numOutputBusChannels=2;
	var <>numBuffers=1024;
	var <>maxNodes=1024;
	var <>maxSynthDefs=1024;

	asOptionsString { // asString confused the Inspector
		var o;
		o = "";
		if (numAudioBusChannels != 128, { 
			o = o ++ " -a " ++ numAudioBusChannels;
		});
		if (numControlBusChannels != 4096, { 
			o = o ++ " -c " ++ numControlBusChannels;
		});
		if (numInputBusChannels != 2, { 
			o = o ++ " -i " ++ numInputBusChannels;
		});
		if (numOutputBusChannels != 2, { 
			o = o ++ " -o " ++ numOutputBusChannels;
		});
		if (numBuffers != 1024, { 
			o = o ++ " -b " ++ numBuffers;
		});
		if (maxNodes != 1024, { 
			o = o ++ " -n " ++ maxNodes;
		});
		if (maxSynthDefs != 1024, { 
			o = o ++ " -d " ++ maxSynthDefs;
		});
		^o
	}
	
	firstPrivateBus { // after the outs and ins
		^numOutputBusChannels + numInputBusChannels
	}
}

Server : Model {
	classvar <>local, <>internal, <>named, <>set;
	
	var <name, <addr;
	var <isLocal, <inProcess;
	var <serverRunning = false;
	var >options;

	var <numUGens=0,<numSynths=0,<numGroups=0,<numSynthDefs=0;

	var alive = false,aliveThread,statusWatcher;
	
	var <window;
	
	*new { arg name, addr, options;
		^super.new.init(name, addr, options)
	}
	init { arg argName, argAddr, argOptions;
		name = argName;
		addr = argAddr;
		options = argOptions ? ServerOptions.new;
		if (addr.isNil, { addr = NetAddr("127.0.0.1", 57110) });
		inProcess = addr.addr == 0;
		isLocal = inProcess || { addr.addr == 2130706433 };
		serverRunning = false;
		named.put(name, this);
		set.add(this);
	}
	
	*initClass {
		named = IdentityDictionary.new;
		set = Set.new;
		internal = Server.new(\internal, NetAddr.new);
		local = Server.new(\localhost, NetAddr("127.0.0.1", 57110));
		// moved to Main-startUp
		//local.makeWindow;
		//internal.makeWindow;
	}
	options { ^(options ?? {options = ServerOptions.new}) }
	sendMsg { arg ... args;
		addr.sendBundle(nil, args);
	}
	sendBundle { arg time ... args;
		addr.performList(\sendBundle, time, args);
	}
	sendSynthDef { arg name;
		var file, buffer;
		file = File("synthdefs/" ++ name ++ ".scsyndef","r");
		if (file.isNil, { ^nil });
		buffer = Int8Array.newClear(file.length);
		file.read(buffer);
		file.close;
		this.sendMsg("/d_recv", buffer);
	}
	loadSynthDef { arg name;
		this.sendMsg("/d_load", "synthdefs/" ++ name ++ ".scsyndef");
	}
	
	serverRunning_ { arg val;
		if (val != serverRunning, {
			serverRunning = val;
			this.changed(\serverRunning);
		});
	}
	
	wait { arg cmdName;
		var resp, routine;
		routine = thisThread;
		resp = OSCresponder(addr, cmdName, { resp.remove; routine.resume(true); });
		resp.add;
	}
	addStatusWatcher {
		statusWatcher = 
			OSCresponder(addr, 'status.reply', { arg time, resp, msg;
				alive = true;
				numUGens = msg.at(2);
				numSynths = msg.at(3);
				numGroups = msg.at(4);
				numSynthDefs = msg.at(5);
				{
					this.serverRunning_(true);
					this.changed(\counts);
					nil // no resched
				}.defer;
			}).add;	
	}
	startAliveThread { arg secs=0.7; // not needed for an inProcess server ?
		^aliveThread ?? {
			this.addStatusWatcher;
			aliveThread = Routine({
				// this thread polls the server to see if it is alive
				4.0.wait;
				loop({
					this.status;
					0.7.wait;
					this.serverRunning = alive;
					alive = false;
				});
			});
			AppClock.play(aliveThread);
			aliveThread
		}
	}
	stopAliveThread {
		aliveThread.stop;
		statusWatcher.remove;
	}
	
	boot {
		if (serverRunning, { "server already running".inform; ^this });
		if (isLocal.not, { "can't boot a remote server".inform; ^this });
		if (inProcess, { 
			"boot in process".inform;
			this.bootInProcess; 
			//alive = true;
			//this.serverRunning = true;
		},{
			//isBooting = true;
			//("./scsynth -u " ++ addr.port ++ " " ++ options.asString).postln;
			unixCmd("./scsynth -u " ++ addr.port ++ " " ++ (if(options.notNil,{options.asOptionsString},"")));
			("booting " ++ addr.port.asString).inform;
		});
		this.notify(true);
	}
	
	quit {
		addr.sendMsg("/quit");
		if (inProcess, { 
			this.quitInProcess;
			"quit done\n".inform;
		},{
			"/quit sent\n".inform;
		});
		alive = false;
		this.serverRunning = false;
	}
	
	status {
		addr.sendMsg("/status");
	}
	
	notify { arg flag=true;
		addr.sendMsg("/notify", flag.binaryValue);
	}	

	// internal server commands
	bootInProcess {
		_BootInProcessServer
		^this.primitiveFailed
	}
	quitInProcess {
		_QuitInProcessServer
		^this.primitiveFailed
	}
	allocSharedControls { arg numControls=1024;
		_AllocSharedControls
		^this.primitiveFailed
	}
	allocSharedBufs { arg numScopeBufs=16, samples=1024, channels=1;
		_AllocSharedSndBufs
		^this.primitiveFailed
	}
	setSharedControl { arg num, value;
		_SetSharedControl
		^this.primitiveFailed
	}
	getSharedControl { arg num;
		_GetSharedControl
		^this.primitiveFailed
	}
	
	////
	makeWindow {
		var w, active;
		var countsViews, ctlr;
		
		if (window.notNil, { ^window.front });
		
		w = window = SCWindow(name.asString ++ " server", Rect(10, named.values.indexOf(this) * 140 + 10, 190, 120));
		w.view.decorator = FlowLayout(w.view.bounds);
		
		active = SCButton(w, Rect(0,0, 80, 24));
		active.states = [["Stopped", Color.white, Color.red],
					["Booting...",Color.black,Color.yellow],
					["Running", Color.black, Color.green]];
		if(serverRunning,{ active.setProperty(\value,2); });
		
		if (isLocal, {
			w.onClose = {
				//OSCresponder.removeAddr(addr);
				//this.stopAliveThread;
				//this.quit;
				window = nil;
				ctlr.remove;
			};
			active.action = { arg view; 
				view.value.postln;
				if(view.value == 1, {
					this.boot;
				});
				if(view.value == 0,{
					this.quit;
				});
				if(view.value == 2,{
					// try abort boot ?
					// this.quit;
					view.setProperty(\value, 0);
				});
			};
		},{	
			active.enabled = false; // look but don't touch
			w.onClose = {
				// but do not remove other responders
				this.stopAliveThread;
				ctlr.remove;
			};
		});
		
		w.view.decorator.nextLine;

		countsViews = 
		["UGens :", "Synths :", "Groups :", "SynthDefs :"].collect({ arg name;
			var label,numView;
			label = SCStaticText(w, Rect(0,0, 80, 15));
			label.string = name;
			label.align = \right;
		
			numView = SCStaticText(w, Rect(0,0, 60, 15));
			numView.string = "?";
			numView.align = \left;
			
			w.view.decorator.nextLine;
			
			numView
		});
		
		w.front;

		ctlr = SimpleController(this)
			.put(\serverRunning, {
				active.setProperty(\value, if(serverRunning,2,0) );
			})
			.put(\counts,{
				countsViews.at(0).string = numUGens;
				countsViews.at(1).string = numSynths;
				countsViews.at(2).string = numGroups;
				countsViews.at(3).string = numSynthDefs;
			});	
		
		this.startAliveThread;
	}
}
