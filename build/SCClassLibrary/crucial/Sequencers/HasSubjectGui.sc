

HasSubjectGui : AbstractPlayerGui {
	//smallGui could not gui the subject
	var subjg;
	guiBody { arg layout;
		layout.startRow;
		subjg = model.subject.gui(layout);
	}
}


PlayerEffectGui : HasSubjectGui {
	
	guiBody { arg layout;
		model.subject.gui(layout);
		model.effect.gui(layout.startRow);
	}
}

PlayerAmpGui : HasSubjectGui {
	var num;

	guiBody {  arg layout;
		layout = this.guify(layout);
		this.smallGui(layout);
		model.subject.gui(layout);
	}

	smallGui { arg layout;
		var l;
		l=this.guify(layout);
		num = NumberEditor(1.0,[0,2.0]);
		num.action_({ arg val; model.amp_(val) });
		num.gui(l);
		if(layout.isNil,{ l.front });
	}
	update { arg changed,changer;
		num.value_(model.amp).changed;
	}
}


// move me...
StreamKrDurGui : HasSubjectGui {

	guiBody { arg layout;
		CXLabel(layout,"delta:");
		model.durations.smallGui(layout);
		layout.startRow;
		subjg = model.values.gui(layout);
	}
}
