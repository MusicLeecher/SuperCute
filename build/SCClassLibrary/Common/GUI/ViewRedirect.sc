ViewRedirect { // Abstract class
	*implClass { ^GUI.scheme.perform(this.key) }
	*new { arg parent, bounds; ^this.implClass.new(parent, bounds) }
	*browse { ^this.implClass.browse }
	*doesNotUnderstand{|selector ... args|	^this.implClass.perform(selector, *args)  }
}


Window : ViewRedirect { 
	*key { ^\window }
	*new { arg name = "panel", bounds, resizable = true, border = true, server, scroll = false;
		^this.implClass.new(name, bounds, resizable, border, server, scroll)
	}
}

CompositeView : ViewRedirect { *key { ^\compositeView }}
ScrollView : ViewRedirect { *key { ^\scrollView }}
HLayoutView : ViewRedirect { *key { ^\hLayoutView }}
VLayoutView : ViewRedirect { *key { ^\vLayoutView }}

Slider : ViewRedirect { *key { ^\slider }}

//Knob : SCSlider {
//}

//Font : ViewRedirect { *key { ^\font }}
Pen : ViewRedirect { *key { ^\pen }}

Stethoscope : ViewRedirect { *key { ^\stethoscope }}
ScopeView : ViewRedirect { *key { ^\scopeView }}
FreqScopeView : ViewRedirect { *key { ^\freqScopeView }} // redirects to SCFreqScope

FreqScope : ViewRedirect { // redirects to SCFreqScopeWindow
	*new { arg width=512, height=300, busNum=0, scopeColor, bgColor;
		^this.implClass.new(width, height, busNum, scopeColor)
		}
	*key { ^\freqScope }
} 

Dialog : ViewRedirect { *key { ^\dialog }}
View : ViewRedirect { *key { ^\view }}

RangeSlider : ViewRedirect { *key { ^\rangeSlider }}
Slider2D : ViewRedirect { *key { ^\slider2D }}
TabletSlider2D : ViewRedirect { *key { ^\tabletSlider2D }}
Button : ViewRedirect { *key { ^\button }}

PopUpMenu : ViewRedirect { *key { ^\popUpMenu }}
StaticText : ViewRedirect { *key { ^\staticText }}
NumberBox : ViewRedirect { *key { ^\numberBox }}
ListView : ViewRedirect { *key { ^\listView }}

DragSource : ViewRedirect { *key { ^\dragSource }}
DragSink : ViewRedirect { *key { ^\dragSink }}
DragBoth : ViewRedirect { *key { ^\dragBoth }}

UserView : ViewRedirect { *key { ^\userView }}
MultiSliderView : ViewRedirect { *key { ^\multiSliderView }}
EnvelopeView : ViewRedirect { *key { ^\envelopeView }}

TextField : ViewRedirect  { *key { ^\textField }}


TabletView : ViewRedirect { *key { ^\tabletView }}
SoundFileView : ViewRedirect { *key { ^\soundFileView }}
MovieView : ViewRedirect { *key { ^\movieView }}
TextView : ViewRedirect  {	*key { ^\textView }}


EZSlider : ViewRedirect  {
	*new { arg parent, bounds, label, controlSpec, action, initVal, 
			initAction=false, labelWidth=80, numberWidth=40 , unitWidth=0, 
			labelHeight=20,  layout=\horz, gap;
		^this.implClass.new(parent, bounds, label, controlSpec, action, initVal, 
			initAction, labelWidth, numberWidth , unitWidth, labelHeight, layout, gap)
	}
		*key { ^\ezSlider }}

EZNumber : ViewRedirect  {

	*new { arg parent, bounds, label, controlSpec, action, initVal, 
			initAction=false, labelWidth=80, numberWidth=40 , unitWidth=0,
			 labelHeight=20,  layout=\horz, gap;
		^this.implClass.new(parent, bounds, label, controlSpec, action, initVal, 
			initAction, labelWidth, numberWidth, unitWidth, labelHeight, layout, gap)
	}
	*key { ^\ezNumber }
}
EZRanger : ViewRedirect  {

	*new {arg parent, bounds, label, controlSpec, action, initVal, 
			initAction=false, labelWidth=60, numberWidth=40, 
			unitWidth=0, labelHeight=20,  layout=\horz, gap;
		^this.implClass.new(parent, bounds, label, controlSpec, action, 
			initVal, initAction, labelWidth, numberWidth, 
				unitWidth, labelHeight, layout, gap)
	}
	*key { ^\ezRanger }
}


