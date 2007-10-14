WiiCalibrationInfo {
	var <accX_zero, <accY_zero, <accZ_zero, <accX_lg, <accY_lg, <accZ_lg;
	var <nAccX_zero, <nAccY_zero, <nAccZ_zero, <nAccX_lg, <nAccY_lg, <nAccZ_lg;
	var <nX_max, <nX_min, <nX_center, <nY_max, <nY_min, <nY_center;

	printOn { | stream |
		stream
		<< this.class.name << $(
		<< "accX_zero: " << accX_zero << ", "
		<< "accY_zero: " << accY_zero << ", "
		<< "accZ_zero: " << accZ_zero << ", "
		<< "accX_lg: " << accX_lg << ", "
		<< "accY_lg: " << accY_lg << ", "
		<< "accZ_lg: " << accZ_lg << ", "
		<< "nunchuk accX_zero: " << nAccX_zero << ", "
		<< "nunchuk accY_zero: " << nAccY_zero << ", "
		<< "nunchuk accZ_zero: " << nAccZ_zero << ", "
		<< "nunchuk accX_lg: " << nAccX_lg << ", "
		<< "nunchuk accY_lg: " << nAccY_lg << ", "
		<< "nunchuk accZ_lg: " << nAccZ_lg << ", "
		<< "nunchuk x_min: " << nX_min << ", "
		<< "nunchuk x_max: " << nX_max << ", "
		<< "nunchuk x_center: " << nX_center << ", "
		<< "nunchuk y_min: " << nY_min << ", "
		<< "nunchuk y_max: " << nY_max << ", "
		<< "nunchuk y_center: " << nY_center
		<< $)
	}
}

WiiMoteIRObject{
	var <>id, <>valid, <>posx, <>posy, <>size;
	var <>action;
}

WiiMote {
	var dataPtr, <spec, <>action, <actionSpec; // <slots
	var <>id;
	var <battery;
	var <ext_type;
	var <>closeAction, <>connectAction, <>disconnectAction;
	var <calibration;
	var <remote_led, <>remote_buttons, <>remote_motion, <>remote_ir;
	var <>nunchuk_buttons, <>nunchuk_motion, <>nunchuk_stick;
	var <>classic_buttons, <>classic_stick1, <>classic_stick2, <>classic_analog;
	var <>dumpEvents = false;
	classvar all;
	classvar < eventLoopIsRunning = false;
	classvar < updateDataTask, <updateTask;
	
	*initClass {
		all = [];
	}

	/*	*startUpdate{ |updtime = 0.005|
		updateTask = Task{ loop{ all.do{ |it| it.update }; updtime.wait; } };
		updateDataTask = Task{
			var err, ext_type, ext_button, ext_ana;
			loop{
				all.do{ |it|
					ext_button = Array.fill( 15, 0 );
					ext_ana = Array.fill( 6, 0 );
					it.prUpdateData( err, it.remote_led, it.remote_buttons, it.remote_motion, it.remote_ir, ext_type, ext_button, ext_ana );
					if ( err == 1, 
						{ it.close },
						{ // no error
							if ( ext_type == 0, 
								{ // nunchuk
									it.nunchuk_buttons = ext_button.copyRange( 0, 1 );
									it.nunchuk_stick = ext_ana.copyRange( 0, 1 );
									it.nunchuk_motion = ext_ana.copyRange( 3,5 );

								});
							if ( ext_type == 1, 
								{ // classic
									it.classic_buttons = ext_button;
									it.classic_stick1 = ext_ana.copyRange( 0, 1 );
									it.classic_stick2 = ext_ana.copyRange( 2, 3 );
									it.classic_analog = ext_ana.copyRange( 4,5 );
								});
						});
				};
				(updtime*10).wait;
			}
		};
		updateTask.play;
		updateDataTask.play;
	}

	*stopUpdate{
		updateTask.stop;
		updateDataTask.stop;
	}
	*/

	deviceSpec {
		^(
			ax: { remote_motion[0] },
			ay: { remote_motion[1] },
			az: { remote_motion[2] },
			ao: { remote_motion[3] },

			bA: { remote_buttons[0] },
			bB: { remote_buttons[1] },
			bOne: { remote_buttons[2] },
			bTwo: { remote_buttons[3] },
			bMinus: { remote_buttons[4] },
			bHome: { remote_buttons[5] },
			bPlus: { remote_buttons[6] },
			bUp: { remote_buttons[7] },
			bDown: { remote_buttons[8] },
			bLeft: { remote_buttons[9] },
			bRight: { remote_buttons[10] },

			px: { remote_ir[0] },
			py: { remote_ir[1] },
			angle: { remote_ir[2] },
			tracking: { remote_ir[3] },

			nax: { nunchuk_motion[0] },
			nay: { nunchuk_motion[1] },
			naz: { nunchuk_motion[2] },
			nao: { nunchuk_motion[3] },

			nsx: { nunchuk_stick[0] },
			nsy: { nunchuk_stick[1] },

			nbZ: { nunchuk_buttons[0] },
			nbC: { nunchuk_buttons[1] },

			cbX: { classic_buttons[0] },
			cbY: { classic_buttons[1] },
			cbA: { classic_buttons[2] },
			cbB: { classic_buttons[3] },
			cbL: { classic_buttons[4] },
			cbR: { classic_buttons[5] },
			cbZL: { classic_buttons[6] },
			cbZR: { classic_buttons[7] },
			cbUp: { classic_buttons[8] },
			cbDown: { classic_buttons[9] },
			cbLeft: { classic_buttons[10] },
			cbRight: { classic_buttons[11] },
			cbMinus: { classic_buttons[12] },
			cbHome: { classic_buttons[13] },
			cbPlus: { classic_buttons[14] },

			csx1: { classic_stick1[0] },
			csy1: { classic_stick1[1] },

			csx2: { classic_stick2[0] },
			csy2: { classic_stick2[1] },

			caleft: { classic_analog[0] },
			caright: { classic_analog[1] }
		)
	}

	*all {
		^all.copy
	}
	*closeAll {
		all.copy.do({ | dev | dev.close });
	}
	*new { |id|
		^super.new.id_(id).prInit();
	}
	isOpen {
		^dataPtr.notNil
	}
	connect{
		^this.prConnect;
	}
	address{
		^this.prAddress;
	}
	/*
	setAddress{ |address|
		^this.prSetAddress( address );
		}*/
	/*	update{
		^this.prUpdate;
		}*/
	close {
		if (this.isOpen) {
			this.prClose;
			//	all.remove(this);
		};
		try { all.remove( this ) };
	}
	setAction{ |key,keyAction|
		actionSpec.put( key, keyAction );
	}
	removeAction{ |key|
		actionSpec.removeAt( key );
	}
	at { | controlName |
		^this.spec.atFail(controlName, {
			Error("invalid control name").throw
		});
	}
	/*
	getLEDState { |id|
		this.prWiiGetLED( remote_led );
		^remote_led[id]
		}*/
	setLEDState { |id,state|
		var r;
		remote_led[id] = state;
		r = this.prWiiSetLED( remote_led );
		//		this.update;
		^r;
	}
	/*
	getExpansion{
		^this.prGetExpansion;
		}*/
	
	enable{ |onoff|
		this.prEnable( onoff );
		//		this.update;
	}
	enableExpansion{ |onoff|
		this.prEnableExpansion( onoff );
		//		this.update;
	}
	enableButtons{ |onoff|
		this.prEnableButtons( onoff );
		//		this.update;
	}
	enableMotionSensor{ |onoff|
		this.prEnableMotionSensor( onoff );
		//		this.update;
	}
	enableIRSensor{ |onoff|
		this.prEnableIRSensor( onoff );
		//		this.update;
	}
	rumble{ |onoff|
		this.prSetVibration( onoff );
		//		this.update;
	}
	/*
	playSample{ |play,freq,vol,sample=0|
		this.prPlaySpeaker( play, freq, vol, sample );
		//		this.update;
	}
	mute{ |onoff|
		this.prMuteSpeaker( onoff );
		//		this.update;
	}
	enableSpeaker{ |onoff|
		this.prEnableSpeaker( onoff );
		//		this.update;
	}
	initSpeaker{ |format=0|
		this.prInitSpeaker( format );
		//		this.update;
		}*/
	/*
	motion{
		this.prWiiGetMotion( remote_motion );
		^remote_motion;
	}

	buttons{
		this.prWiiGetButtons( remote_buttons );
		^remote_buttons;
	}

	ir{
		this.prWiiGetIR( remote_ir );
		^remote_ir;
	}


	nunchukMotion{
		this.prWiiGetNunchukMotion( nunchuk_motion );
		^nunchuk_motion;
	}

	nunchukButtons{
		this.prWiiGetNunchukButtons( nunchuk_buttons );
		^nunchuk_buttons;
	}

	nunchukJoy{
		this.prWiiGetNunchukJoy( nunchuk_stick );
		^nunchuk_stick;
	}
	*/

	*start{ |updtime=0.05|
		UI.registerForShutdown({
			this.closeAll;
			this.prStop;
		});
		this.prStart( updtime );
	}

	*discover{ 
		var newid, newwii;
		newid = all.size;
		newwii = WiiMote.new;
		//		"discovering WIIs: this may take some time".postln;
		//		all = Array.fill( maxdevices, {|i| WiiMote.new(i) } );
		//		Routine({ 
		//			0.5.wait;
		this.prDiscover( newid, all ); 
		//			0.5.wait;
		//			all.do{ |it| if ( it.isOpen.not, { it.close } ) };
		//		}).play;
		^this.all;
	}

	*stop{
		this.prStop;
	}

	// PRIVATE
	prInit {
		remote_led = Array.fill( 4, 0 );
		remote_buttons = Array.fill( 11, 0 );
		remote_motion = Array.fill( 4, 0 );
		remote_ir = Array.fill( 4, { WiiMoteIRObject.new } );
		nunchuk_buttons = Array.fill( 2, 0 );
		nunchuk_motion = Array.fill( 4, 0 );
		nunchuk_stick = Array.fill( 2, 0 );
		classic_buttons = Array.fill( 15, 0 );
		classic_stick1 = Array.fill( 2, 0 );
		classic_stick2 = Array.fill( 2, 0 );
		classic_analog = Array.fill( 2, 0 );

		this.prOpen;

		all = all.add(this);
		closeAction = {};
		connectAction = {};
		disconnectAction = {};

		//		//		this.prWiiGetLED( remote_led );
		//		calibration = this.prCalibration(WiiCalibrationInfo.new);

 		spec = this.deviceSpec;
		actionSpec = IdentityDictionary.new;
	}
	*prStart { |updtime=0.05|
		//eventLoopIsRunning = true;
		_Wii_Start;
		^this.primitiveFailed
	}
	/*
	prUpdate{
		_Wii_Update;
		^this.primitiveFailed
		}*/
	/*	prUpdateData{ |readError,r_led,r_but,r_acc,e_but,e_ana|
		_Wii_UpdateData;
		^this.primitiveFailed
		}*/
	*prStop {
		//eventLoopIsRunning = false;
		_Wii_Stop;
		^this.primitiveFailed
	}
	*prDiscover { |newid,alldevices|
		//eventLoopIsRunning = true;
		_Wii_Discover;
		("newid "++newid).postln;
		all = alldevices.keep( newid+1 );
		//		all = alldevices;
		^this.primitiveFailed
	}
	prOpen { 
		_Wii_Open;
		^this.primitiveFailed
	}
	prClose {
		_Wii_Close;
		^this.primitiveFailed
	}
	prAddress { |address|
		_Wii_Address;
		^this.primitiveFailed
	}
	/*
	prSetAddress { |address|
		_Wii_SetAddress;
		^this.primitiveFailed
		}*/
	prConnect {
		_Wii_Connect;
		^this.primitiveFailed
	}
	prDisconnect {
		_Wii_Disconnect;
		^this.primitiveFailed
	}
	prCalibration { |calib|
		_Wii_Calibration;
		^this.primitiveFailed
	}
	/*	prGetExpansion {
		_Wii_GetExpansion;
		^this.primitiveFailed
	}
	prGetBattery {
		_Wii_GetBattery;
		^this.primitiveFailed
		}*/
	prEnable { |onoff|
		_Wii_Enable;
		^this.primitiveFailed
	}
	prEnableExpansion { |onoff|
		_Wii_EnableExpansion;
		^this.primitiveFailed
	}
	prEnableIRSensor { |onoff|
		_Wii_EnableIRSensor;
		^this.primitiveFailed
	}
	prEnableMotionSensor { |onoff|
		_Wii_EnableMotionSensor;
		^this.primitiveFailed
	}
	prEnableButtons { |onoff|
		_Wii_EnableButtons;
		^this.primitiveFailed
	}
	prSetVibration { |onoff|
		_Wii_SetVibration;
		^this.primitiveFailed
	}
	/*
	prPlaySpeaker { |play, freq, vol, sample |
		_Wii_PlaySpeaker;
		^this.primitiveFailed
	}
	prMuteSpeaker { |onoff |
		_Wii_MuteSpeaker;
		^this.primitiveFailed
	}
	prInitSpeaker { |format|
		_Wii_InitSpeaker;
		^this.primitiveFailed
	}
	prEnableSpeaker { |onoff |
		_Wii_EnableSpeaker;
		^this.primitiveFailed
	}
	*/
	/*
	prWiiGetMotion{ | motion |
		_Wii_GetMotion;
		^this.primitiveFailed;
	}
	prWiiGetButtons{ | motion |
		_Wii_GetButtons;
		^this.primitiveFailed;
	}
	prWiiGetIr{ | motion |
		_Wii_GetIR;
		^this.primitiveFailed;
	}
	prWiiGetNunchukMotion{ | motion |
		_Wii_GetNunchukMotion;
		^this.primitiveFailed;
	}
	prWiiGetNunchukJoy{ | motion |
		_Wii_GetNunchukJoy;
		^this.primitiveFailed;
	}
	prWiiGetNunchukButtons{ | motion |
		_Wii_GetNunchukButtons;
		^this.primitiveFailed;
	}
	*/
	/*	prWiiGetLED { | states |
		_Wii_GetLED;
		^this.primitiveFailed
		}*/
	prWiiSetLED { |states|
		_Wii_SetLED;
		^this.primitiveFailed
	}

	prHandleBatteryEvent{ |batlevel|
		battery = batlevel;
		// battery action
	}

	prHandleExtensionEvent{ |exttype|
		ext_type = exttype;
	}

	prHandleButtonEvent{ |buttonData|
		//		buttonData are bits that decode to separate buttons (do in Primitive internally, and pass on Array?
		//("handle button Event"+buttonData).postln;
		remote_buttons = buttonData;

		[ \bA, \bB, \bOne, \bTwo, \bMinus, \bHome, \bPlus, \bUp, \bDown, \bLeft, \bRight ].do{ |key|
			actionSpec.at( key ).value( spec.at(key).value );
			if ( dumpEvents, { (key + spec.at(key).value.round(0.00001)).postln; });
		}
	}

	prHandleNunchukEvent{ |nunchukButtons, nunJoyX, nunJoyY, nunAccX, nunAccY, nunAccZ|

		// buttonData are bits that decode to separate buttons (do in Primitive)
		nunchuk_buttons = nunchukButtons;
		nunchuk_motion = [ nunAccX, nunAccY, nunAccZ, 0 ];
		nunchuk_stick = [ nunJoyX, nunJoyY ];		

		[ \nax, \nay, \naz, \nsx, \nsy, \nbZ, \nbC ].do{ |key|
			actionSpec.at( key ).value( spec.at(key).value );
			if ( dumpEvents, { (key + spec.at(key).value.round(0.00001)).postln; });
		}
	}

	prHandleClassicEvent{ |clasButtons, clasJoy1X, clasJoy1Y, clasJoy2X, clasJoy2Y, clasL, clasR|

		// buttonData are bits that decode to separate buttons (do in Primitive)
		classic_buttons = clasButtons;
		classic_stick1 = [clasJoy1X, clasJoy1Y];
		classic_stick2 = [clasJoy2X, clasJoy2Y];
		classic_analog = [clasL, clasR];

		[ \cbX, \cbY, \cbA, \cbB, \cbL, \cbR, \cbZL, \cbZR, \cbUp, \cbDown, \cbLeft, \cbRight, \cbMinus, \cbPlus, \csx1, \csy1, \csx2, \csy2, \caleft, \caright  ].do{ |key|
			actionSpec.at( key ).value( spec.at(key).value );
			if ( dumpEvents, { (key + spec.at(key).value.round(0.00001)).postln; });
		}
	}

	prHandleIREvent{ |id,valid,posx,posy,size|
		// make ir an array of WiiMoteIRObject's and assign data to right IR object, and do actions
		remote_ir[id].valid_( valid ).posx_( posx ).posy_( posy ).size_( size ).action.value;
	}

	prHandleAccEvent{ |accX,accY,accZ|
		remote_motion = [ accX, accY, accZ, 0 ];
		// actions:
		[ \ax, \ay, \az ].do{ |key|
			actionSpec.at( key ).value( spec.at(key).value );
			if ( dumpEvents, { (key + spec.at(key).value.round(0.00001)).postln; });
		}
	}

	prHandleEvent { 
		| buttonData, posX, posY, angle, tracking, accX, accY, accZ, orientation, extType, eButtonData, eData1, eData2, eData3, eData4, eData5, eData6 |
		//		| buttonData, posX, posY, angle, tracking, accX, accY, accZ, orientation, cButtonData, cStickX1, cStickY1, cStickX2, cStickY2, cAnalogL, cAnalogR, nButtonData, nStickX, nStickY, nAccX, nAccY, nAccZ, nOrientation |
		//		"WII: handling event\n".postln;
		
		//		remote_buttons.do{ |it,i| remote_buttons[i] = buttonData.bitTest( i ).asInteger };
		remote_motion = [ accX, accY, accZ, orientation ];
		remote_ir = [ posX, posY, angle, tracking ];
		if ( extType == 0, {
			//			nunchuk_buttons.do{ |it,i| nunchuk_buttons[i] = eButtonData.bitTest( i ).asInteger };
			nunchuk_motion = [ eData3, eData4, eData5, eData6 ];
			nunchuk_stick = [ eData1, eData2 ];
		});
		if ( extType == 1, {
			//			classic_buttons.do{ |it,i| classic_buttons[i] = eButtonData.bitTest( i ).asInteger };
			classic_stick1 = [eData1, eData2];
			classic_stick2 = [eData3, eData4];
			classic_analog = [eData5, eData6];
		});

		// event callback
		spec.keysValuesDo{ |key,val,i|
			actionSpec.at( key ).value( val.value );
			if ( dumpEvents, { (key + val.value.round(0.00001)).postln; });
		}
	}

	prReadError{
		this.close;
		"WiiMote read error".warn;
		closeAction.value;
	}

	prConnectAction{
		"WiiMote connected".postln;
		connectAction.value;
	}

	prDisconnectAction{
		"WiiMote disconnected!".warn;
		disconnectAction.value;
	}
}

// EOF