Prewrite : FilterPattern {
	var <>dict, <>levels=1;
	*new { arg pattern, dict, levels;
		^super.new(pattern).dict_(dict).levels_(levels)
	}
	rewriteList { arg list, inval, level;
		var newlist;
		if (level == 0, { 
			// if at bottom level, then embed all items in the stream
			if (list.isKindOf(SequenceableCollection), {
				// isKindOf is necessary because Integer.do would do the wrong thing..
				list.do({ arg item; 
					inval = item.embedInStream(inval); 
				});
			},{
				inval = list.embedInStream(inval);
			});
		},{
			if (list.isKindOf(SequenceableCollection), {
				// isKindOf is necessary because Integer.do would do the wrong thing..
				list.do({ arg item;					
					// lookup item in rewrite dictionary
					newlist = dict.at(item); 
					
					// found an entry ?
					if (newlist.notNil, {
						// do another level of rewriting
						inval = this.rewriteList( newlist, inval, level - 1 );
					},{
						// has no dictionary entry, so embed in stream
						inval = item.embedInStream(inval);
					});
				});
			},{
				// lookup item in rewrite dictionary
				newlist = dict.at(list); 
				
				// found an entry ?
				if (newlist.notNil, {
					// do another level of rewriting
					inval = this.rewriteList( newlist, inval, level - 1 );
				},{
					// has no dictionary entry, so embed in stream
					inval = list.embedInStream(inval);
				});
			});
		});
		^inval;
	}
	asStream { 
		var stream;
		
		stream = pattern.asStream;
		
		^Routine.new({ arg inval;
			var outval;
			while({
				outval = stream.next(inval);
				outval.notNil;
			},{
				inval = this.rewriteList(outval, inval, levels);
			});
		}, 2048)
	}
}

