
ObjectGui : SCViewAdapter { // aka AbstractController

	var <>model;

	guiBody { arg layout;
		// implement this in your subclass
	}
		
	*new { arg model;
		var new;
		new = super.new;
		new.model_(model);
		model.addDependant(new);
		^new
	}
	guify { arg layout,bounds,title;
		if(layout.isNil,{
			layout = MultiPageLayout(title ?? {model.asString.copyRange(0,50)},bounds);
		},{
			layout = layout.asPageLayout(title,bounds);
		});
		// i am not really a view in the hierarchy
		layout.removeOnClose(this);
		^layout
	}
	prClose {
		[this,model].debug("prClose");
		this.remove(false);
	}
	remove { arg removeView=false;
		model.removeDependant(this);
		if(removeView,{
			view.remove;
			view = nil;		
		});
	}
	removeView {
		var parent;
		this.remove;
		parent = view.parent;
		view.remove;
		parent.refresh;
		view = nil;
	}

	gui { arg lay, bounds ... args;
		var layout;
		layout=this.guify(lay,bounds);
		layout.flow({ arg layout;
			view = layout;
			this.writeName(layout);
			this.performList(\guiBody,[layout] ++ args);
		},bounds).background_(this.background);
		//if you created it, front it
		if(lay.isNil,{ layout.resizeToFit.front });
	}
	topGui { arg ... args;
		this.performList(\gui, args);
	}

	background { ^Color.yellow(0.2,0.15) }
	
	writeName { arg layout;
		var n;
		n = model.asString;
		InspectorLink.icon(model,layout);
		SCDragSource(layout,Rect(0,0,(n.size * 7.5).max(70),17))
			.stringColor_(Color.new255(70, 130, 200))
			.background_(Color.white)
			.align_(\center)
			.beginDragAction_({ model })
			.object_(n);	
	}
	
	saveConsole { arg layout;
		^SaveConsole(model,"",layout).save.saveAs.print;
	}
	
	// a smaller format gui, defaults to the tileGui
	smallGui { arg layout;
		this.guify(layout);
		Tile(model,layout);
	}
	
}

ModelImplementsGuiBody : ObjectGui {

	gui { arg lay, bounds ... args;
		var layout;
		layout=this.guify(lay,bounds);
		layout.flow({ arg layout;
			view = layout;
			this.writeName(layout);
			model.performList(\guiBody,[layout] ++ args);
		},bounds).background_(this.background);
		//if you created it, front it
		if(lay.isNil,{ layout.resizeToFit.front });
	}
}


