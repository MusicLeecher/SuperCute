
NetAddr {
	var <addr=0, <>port=0, <hostname, <socket;
	classvar connections;

	*initClass {
		connections = IdentityDictionary.new;
		UI.registerForShutdown({
			this.disconnectAll;
		});
	}

	*new { arg hostname, port=0;
		var addr;
		addr = if (hostname.notNil,{ hostname.gethostbyname },{ 0 });
		^super.newCopyArgs(addr, port, hostname);
	}
	*fromIP { arg addr, port=0;
		^super.newCopyArgs(addr, port, addr.asIPString)
	}

	*useDoubles_ { arg flag = false;
		_NetAddr_UseDoubles
		^this.primitiveFailed;
	}
	*disconnectAll {
		connections.keys.do({ | netAddr |
			netAddr.disconnect;
		});
	}

	hostname_ { arg inHostname;
		hostname = inHostname;
		addr = inHostname.gethostbyname;
	}
	

	sendRaw { arg rawArray;
		_NetAddr_SendRaw
		^this.primitiveFailed;
	}
	sendMsg { arg ... args;
		_NetAddr_SendMsg
		^this.primitiveFailed;
	}
	sendBundle { arg time ... args;
		_NetAddr_SendBundle
		^this.primitiveFailed;
	}
	bundleSize { arg time ... args;
		_NetAddr_BundleSize; 
		^this.primitiveFailed;
	}
	msgSize { arg ... args;
		_NetAddr_BundleSize; 
		^this.primitiveFailed;
	}
	
	isConnected {
		^socket.notNil
	}
	connect { | disconnectHandler |
		if (this.isConnected.not) {
			this.prConnect;
			connections.put(this, disconnectHandler);
		};
	}
	disconnect {
		if (this.isConnected) {
			this.prDisconnect;
			this.prConnectionClosed;
		};
	}
	
	== { arg that; 
		^that respondsTo: #[\port, \addr] 
			and: { this.port == that.port and: { this.addr == that.addr} }
	}
	hash { arg that;
		^addr.hash bitXor: port.hash
	} 

	ip {
		^addr.asIPString
	}

	printOn { | stream |
		super.printOn(stream);
		stream << $( << this.ip << ", " << port << $)
	}
	storeOn { | stream |
		super.storeOn(stream);
		stream << $( << this.ip << ", " << port << $)
	}

	// PRIVATE
	prConnect {
		_NetAddr_Connect
		^this.primitiveFailed;		
	}
	prDisconnect {
		_NetAddr_Disconnect
		^this.primitiveFailed;
	}
	prConnectionClosed {
		// called when connection is closed either by sclang or by peer
		socket = nil;
		connections.removeAt(this).value(this);
	}
	recover { ^this }
}

BundleNetAddr : NetAddr {
	var <saveAddr, <>bundle;

	*copyFrom { arg addr, bundle;
		^super.newCopyArgs(addr.addr, addr.port, addr.hostname, addr.socket, addr, bundle);
	}

	sendRaw { arg rawArray;
		bundle = bundle.add( rawArray );
	}
	sendMsg { arg ... args;
		bundle = bundle.add( args );
	}
	sendBundle { arg time ... args;
		bundle = bundle.addAll( args );
	}
	
	recover {
		^saveAddr.recover
	}
}

