
Editor {
	var  <>action, // { arg value,theEditor; }
		<>value, <patchOut;
	
	guiClass { ^ObjectGui }

	storeOn { arg stream;
		value.storeOn(stream)
	}

	next { ^this.value }// Object would return this
	poll { ^value }
	
	setPatchOut { arg po; patchOut = po }

	prepareToBundle { arg group,bundle;
		if(patchOut.isNil,{
			patchOut = ScalarPatchOut(this);
		});
	}
	synthArg { ^this.poll }
	instrArgFromControl { arg control;
		^control
	}
	
	editWithCallback { arg callback;
		ModalDialog({ arg layout;
			this.gui(layout);
		},{
			callback.value(value);
		})
	}
	
}

NumberEditor : Editor {
	
	var <spec;
	
	*new { arg value=1.0,spec='amp';
		^super.new.init(value,spec)
	}
	init { arg val,aspec;
		spec = aspec.asSpec ?? {ControlSpec.new};
		this.value_(spec.constrain(val));
	}
	activeValue_ { arg val;
		this.value_(val);
		action.value(value);
	}
	spec_ { arg aspec;
		spec = aspec.asSpec;
		value = spec.constrain(value);
		this.changed(\spec);
	}

	addToSynthDef { arg synthDef,name;
		synthDef.addInstrOnlyArg(name,this.synthArg)
	}
	instrArgFromControl { arg control;
		^value
	}
	instrArgRate { ^\scalar }

	guiClass { ^NumberEditorGui }

}


// for controls that won't talk to the server
KrNumberEditor : NumberEditor { 

 	var lag=0.05;
 
	instrArgRate { ^\control }

 	canDoSpec { arg aspec; ^aspec.isKindOf(ControlSpec) }

	addToSynthDef {  arg synthDef,name;
		synthDef.addKr(name,this.synthArg);
	}
	instrArgFromControl { arg control;
		^control
	}
//	instrArgFromControl { arg control;
//		//should request a LagControl
//		if(lag > 0.0,{
//			^Lag.kr(control,lag)
//		},{
//			^control
//		})
//	}
	prepareToBundle { arg group,bundle;
		if(patchOut.isNil,{
			patchOut = UpdatingScalarPatchOut(this,enabled: false);
		});
	}
	connectToPatchIn { arg patchIn,needsValueSetNow = true;
		patchOut.connectTo(patchIn,needsValueSetNow);
	}
	stop {
		patchOut.free;
	}
	free { this.stop }	
 
}

IntegerEditor : NumberEditor {

	value_ { arg val,changer;
		value = val.asInteger;
		this.changed(\value,changer);
	}
}

// paul.crabbe@free.fr
PopUpEditor : KrNumberEditor {

	var <>labels, <>values,<selectedIndex;
	
	*new { arg initVal,labels,values;
		^super.new.pueinit(initVal,values,labels)
	}
	init {} // should be a shared superclass above this
			// not using spec or constrain
	pueinit { arg initVal,v,l;
		value = initVal;
		if(l.isNil,{
			values = v ?? { Array.series(10,0,0.1) };
			labels = (l ? values).collect({ arg l; l.asString });
		},{
			values = v ?? { Array.series(labels.size,0,1) };
			labels = l.collect({ arg l; l.asString });
		});	
		selectedIndex = values.indexOf(value) ? 0;
	}
	value_ { arg val;
		value = val;
		selectedIndex = values.indexOf(value) ? 0;
	}
	selectByIndex { arg index;
		value = values.at(index);
		selectedIndex = index;
	}
	guiClass { ^PopUpEditorGui }
}


BooleanEditor : NumberEditor {

	*new { arg val=false;
		^super.new.value_(val)
	}
	// value returns true/false

	guiClass { ^BooleanEditorGui }
}

