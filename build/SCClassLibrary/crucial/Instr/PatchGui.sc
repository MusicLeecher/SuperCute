
PatchGui : AbstractPlayerGui {
	
	guiBody { arg layout;
		var bounds, maxHeight,vl;
		bounds = layout.bounds;
		maxHeight = bounds.height - 20 - (model.args.size * 15) / model.args.size;
		//maxHeight.debug;
		
		Tile(this.model.instr,layout);
		//vl = SCVLayoutView(layout.startRow,layout.decorator.indentedRemaining);
		vl = layout;
		model.args.do({ arg a,i;
			var gui,disclosed=true,box;
			layout.startRow;
			//ArgNameLabel(model.instr.argNames.at(i),layout);
			SCDragSink(vl,Rect(0,0,100,15))
				.background_(Color( 0.47843137254902, 0.72941176470588, 0.50196078431373 ))
				.font_(Font("Helvetica",10))
				.align_(\left)
				.acceptDrag_({  
					model.instr.specs.at(i).canAccept(SCView.currentDrag);
				})
				.object_(model.instr.argNames.at(i))
				.action_({ arg sink;
					// assumes to copy the object
					model.setInput(i,sink.object.copy);
					sink.object = model.instr.argNames.at(i); // don't change the name
					if(gui.notNil,{
						gui.remove(true);
						// expand the box
						//layout.bounds = layout.bounds.resizeTo(1000,1000);
						box.bounds = box.bounds.resizeTo(900,900);
						gui = model.args.at(i).gui(box);
						box.resizeToFit(true,true);
						//layout.reflowAll;
					});
				});

			box = vl.flow({ arg layout;
				//layout.asView.setProperty(\maxHeight, maxHeight);
				
				if(a.tryPerform('path').notNil,{
					Tile(a,layout);
				},{
					gui = a.gui(layout);/*
					ToggleButton(layout,"�",
						{  gui = model.args.at(i).gui(box); },
						{  gui.remove(true); box.refresh;  },
						true
					);
					layout.startRow;
					box = layout.flow({ arg box;
							gui = model.args.at(i).gui(box);
						})*/
				});
			})
		});
	}
}
 
InstrSpawnerGui : PatchGui {

	guiBody { arg layout;
		super.guiBody(layout);
		layout.startRow;
		CXLabel(layout,"delta pattern:");
		model.delta.gui(layout);
	}
}