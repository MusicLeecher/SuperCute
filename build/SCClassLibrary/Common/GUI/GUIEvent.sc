
// not currently used
	
GUIEvent {
	var <>screen, <>mouseInView, <>cmdKey, <>optionKey, <>controlKey, <>shiftKey, <>capsLockKey;
	*new { arg screen, mouseInView, cmdKey=false, optionKey=false, 
				controlKey=false, shiftKey=false, capsLockKey=false;
		^super.newCopyArgs(screen, mouseInView, 
				cmdKey, optionKey, controlKey, shiftKey, capsLockKey);
	}
}
	
MouseEvent : GUIEvent {
	var <>where;
	*new { arg screen, where, mouseInView, cmdKey=false, optionKey=false, 
				controlKey=false, shiftKey=false, capsLockKey=false;
		^super.new(screen, mouseInView, cmdKey, optionKey, controlKey, 
					shiftKey, capsLockKey).where_(where);
	}
}
	
KeyEvent : GUIEvent {
	var <>char;
	*new { arg screen, char, mouseInView, cmdKey=false, optionKey=false, 
				controlKey=false, shiftKey=false, capsLockKey=false;
		^super.new(screen, mouseInView, cmdKey, optionKey, controlKey, 
					shiftKey, capsLockKey).char_(char);
	}
}

MouseTracker {
	var <>movedAction, <>upAction;
	*new { arg movedAction, upAction;
		^super.new.movedAction_(movedAction).upAction_(upAction)
	}
	mouseMoved { arg where, event;
		movedAction.value(where, event)
	}
	mouseUp { arg where, event;
		upAction.value(where, event)
	}
}
