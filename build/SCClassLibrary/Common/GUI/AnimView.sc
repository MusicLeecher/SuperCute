
AnimView {
	var <>function, <>view;
	*new { arg function, view;
		^super.newCopyArgs(function, view)
	}
	draw {
		function.value(view);
	}
	mouseDown { arg where, event;
		^view.mouseDown(where, event)
	}
	mouseOver { arg where, event;
		^view.mouseOver(where, event)
	}
	layout { arg argBounds;
		^view.layout(argBounds)
	}
	getLayoutSize {
		^view.getLayoutSize
	}
}
