Inspector {
	classvar <allInspectors;
	
	var <object, <window, vpos=0;
	
	*new { arg object;
		var inspector;
		inspector = this.inspectorFor(object);
		if (inspector.isNil, { 
			inspector = super.newCopyArgs(object).init
		});
		inspector.window.front; 
		^inspector 
	}
	*inspectorFor { arg object;
		var inspector;
		allInspectors.notNil.if({
			inspector = allInspectors.detect({ arg item;
				item.object === object
			});
		});
		^inspector
	}
	closed {
		allInspectors.remove(this);
	}
	lineHeight { ^28 }
	makeWindow {
		var bounds;
		bounds = Rect(80, 80, 376, this.lineHeight * (this.numLines + 1) + 16);

		window = SCWindow(object.class.name.asString ++ " inspector", bounds);
		window.onClose = Message(this, \closed);
	}
}

ObjectInspector : Inspector {
	
	var stringView, slotInspectors;
	
	numLines {
		^min(20, object.slotSize); // don't display too many lines
	}
	
	init {		
		allInspectors = allInspectors.add(this);
						
		this.makeWindow;
		
		vpos = 4;
		
		this.makeHead;
		
		this.makeBody;
				
		window.refresh;
	}
	
	makeHead {
		var view;
		view = SCButton(window, Rect(8, vpos, 128, 24));
		view.states = [[object.class.name]];
		view.action = Message(object.class, \inspect);
		
		if (object.mutable, {
			view = SCButton(window, Rect(140, vpos, 50, 24));
			view.states = [["update"]];
			view.action = Message(this, \update);
		});
		
		view = SCDragSource(window, Rect(194, vpos, 174, 24));
		view.object = object;
		view.resize = 2;
		stringView = view;
		
		vpos = vpos + this.lineHeight;
	}
	
	makeBody {
		this.numLines.do({ arg i;
			slotInspectors = slotInspectors.add(
				SlotInspector(this, i, vpos)
			);			
			vpos = vpos + this.lineHeight;
		});
	}
	
	update {
		stringView.object = object;
		slotInspectors.do({ arg slotinsp;
			slotinsp.update;
		});
	}
}

StringInspector : ObjectInspector {
	// don't bother with the array of Chars.
	numLines { ^0 }
}

ClassInspector : ObjectInspector {
	makeHead {
		var view;
		view = SCButton(window, Rect(8, vpos, 128, 24));
		view.states = [[object.class.name]];
		view.action = Message(object.class, \inspect);
		
		view = SCButton(window, Rect(140, vpos, 50, 24));
		view.states = [["edit"]];
		view.action = Message(object, \openCodeFile);
		
		if (object.superclass.notNil, {
			view = SCButton(window, Rect(194, vpos, 70, 24));
			view.states = [["superclass"]];
			view.action = Message(object.superclass, \inspect);
		});
		
		view = SCDragSource(window, Rect(268, vpos, 96, 24));
		view.object = object;
		view.resize = 2;
		stringView = view;
		
		vpos = vpos + this.lineHeight;
	}

}

FunctionDefInspector : ObjectInspector {
	openSuper {
		object.superclass.inspect;
	}
	makeHead {
		var view;
		view = SCButton(window, Rect(8, vpos, 128, 24));
		view.states = [[object.class.name]];
		view.action = Message(object.class, \inspect);
				
		if (object.code.notNil, {
			view = SCButton(window, Rect(194, vpos, 70, 24));
			view.states = [["dump code"]];
			view.action = Message(object, \dumpByteCodes);
		});
		
		view = SCDragSource(window, Rect(268, vpos, 96, 24));
		view.object = object;
		view.resize = 2;
		stringView = view;
		
		vpos = vpos + this.lineHeight;
	}
}

MethodInspector : ObjectInspector {
	openSuper {
		object.superclass.inspect;
	}
	makeHead {
		var view;
		view = SCButton(window, Rect(8, vpos, 128, 24));
		view.states = [[object.class.name]];
		view.action = Message(object.class, \inspect);
		
		view = SCButton(window, Rect(140, vpos, 50, 24));
		view.states = [["edit"]];
		view.action = Message(object, \openCodeFile);
		
		if (object.code.notNil, {
			view = SCButton(window, Rect(194, vpos, 70, 24));
			view.states = [["dump code"]];
			view.action = Message(object, \dumpByteCodes);
		});
		
		view = SCDragSource(window, Rect(268, vpos, 96, 24));
		view.object = object;
		view.resize = 2;
		stringView = view;
		
		vpos = vpos + this.lineHeight;
	}
}

SlotInspector {
	var <object, <>index, <key, <slotKeyView, <slotValueView, <inspectButton;
	
	*new { arg inspector, index, vpos;
		^super.newCopyArgs(inspector.object, index).init(inspector, vpos)
	}
	init { arg inspector, vpos;
		var w, class, hasGetter, hasSetter, vbounds, value;
		
		w = inspector.window;
		key = object.slotKey(index);
		class = object.class;
		
		slotKeyView = SCStaticText(w, Rect(8, vpos, 110, 24));
		slotKeyView.align = \right;
		
		if (key.isKindOf(Symbol), {
			hasGetter = class.findMethod(key).notNil;
			hasSetter = class.findMethod(key.asSetter).notNil && object.mutable;
		},{
			hasGetter = true;
			hasSetter = object.mutable;
		});
		vbounds = Rect(122, vpos, 218, 24);
		if (hasSetter, {
			if (hasGetter, {
				slotValueView = SCDragBoth(w, vbounds);
			},{
				slotValueView = SCDragSink(w, vbounds);
			});
			slotValueView.action = Message(this, \setSlot);
		},{
			if (hasGetter, {
				slotValueView = SCDragSource(w, vbounds);
			},{
				slotValueView = SCStaticText(w, vbounds);
			});
		});
		slotValueView.resize = 2;

		inspectButton = SCButton(w, Rect(344, vpos, 24, 24));
		inspectButton.states = [["I"]];
		inspectButton.action = Message(this, \inspectSlot);
		inspectButton.resize = 3;
		inspectButton.visible = object.slotAt(index).canInspect;
		this.update;
	}
	update {
		key = object.slotKey(index);
		slotKeyView.string = key;
		slotValueView.object = object.slotAt(index);
		inspectButton.visible = object.slotAt(index).canInspect;
	}
	inspectSlot {
		object.slotAt(index).inspect;
	}
	setSlot {
		if (key.isKindOf(Symbol), {
			object.perform(key.asSetter, SCView.currentDrag);
		},{
			object.put(key, SCView.currentDrag);
		});
		this.update;
	}
}


