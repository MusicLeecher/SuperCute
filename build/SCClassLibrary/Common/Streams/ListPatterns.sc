ListPattern : Pattern {
	var <>list, <>repeats=1;
	
	*new { arg list, repeats=1;
		^super.new.list_(list).repeats_(repeats)
	}
	copy {
		^super.copy.list_(list.copy)
	}
	storeParamsOn { arg stream;
		stream << "(" <<<* [ list, repeats ] << ")";
	}
}

Pseq : ListPattern {
	var <>offset;
	*new { arg list, repeats=1, offset=0;
		^super.new(list, repeats).offset_(offset)
	}
	asStream { 
		^Routine.new({ arg inval;
			var item, offsetValue;
			offsetValue = offset.value;
			if (inval.eventAt('reverse') == true, {
				repeats.value.do({ arg j;
					list.size.reverseDo({ arg i;
						item = list @@ (i + offsetValue);
						inval = item.embedInStream(inval);
					});
				});
			},{
				repeats.value.do({ arg j;
					list.size.do({ arg i;
						item = list @@ (i + offsetValue);
						inval = item.embedInStream(inval);
					});
				});
			});
		});
	}
	storeParamsOn { arg stream;
		stream << "(" <<<* [ list, repeats, offset ] << ")";
	}
}

Pser : Pseq {
	asStream {
		^Routine.new({ arg inval;
			var item, offsetValue;
			
			offsetValue = offset.value;
			if (inval.eventAt('reverse') == true, {
				repeats.value.reverseDo({ arg i;
					item = list @@ (i + offsetValue);
					inval = item.embedInStream(inval);
				});
			},{
				repeats.value.do({ arg i;
					item = list @@ (i + offsetValue);
					inval = item.embedInStream(inval);
				});
			});
		});
	}
}	

Pshuf : ListPattern {
	asStream { 
		^Routine.new({ arg inval;
			var localList, item, stream;
			
			localList = list.copy.scramble;
			repeats.value.do({ arg j;
				localList.size.do({ arg i;
					item = localList @@ i;
					inval = item.embedInStream(inval);
				});
			});
		});
	}
}

Prand : ListPattern {
	asStream { 
		^Routine.new({ arg inval;
			var item;
			
			repeats.value.do({ arg i;
				item = list.at(list.size.rand);
				inval = item.embedInStream(inval);
			});
		});
	}
}		

Pxrand : ListPattern {
	asStream { 
		^Routine.new({ arg inval;			
			var item, index, size;
			index = list.size.rand;
			repeats.value.do({ arg i;
				size = list.size;
				index = (index + (size - 1).rand + 1) % size;
				item = list.at(index);
				inval = item.embedInStream(inval);
			});
		});
	}
}		

Pwrand : ListPattern {
	var <>weights;
	*new { arg list, weights, repeats=1;
		^super.new(list, repeats).weights_(weights)
	}
	asStream { 
		^Routine.new({ arg inval;
			var item;
			repeats.value.do({ arg i;
				item = list.at(weights.windex);
				inval = item.embedInStream(inval);
			});
		});
	}
	storeParamsOn { arg stream;
		stream << "(" <<<* [ list, weights, repeats ] << ")";
	}
}		


Pfsm : ListPattern {
	asStream { 
		^Routine.new({ arg inval;
			var item, index=0, maxState;
			maxState = ((list.size - 1) div: 2) - 1;
			repeats.value.do({
				index = 0;
				while({
					index = list.at(index).choose.clip(0, maxState) * 2 + 2;
					item = list.at(index - 1);
					inval = item.embedInStream(inval);
					inval.notNil;
				});
			});
		});
	}
}	

	
//Ppar : ListPattern {
//	initStreams { arg priorityQ;
//		list.do({ arg pattern, i; 
//			priorityQ.put(0.0, pattern.asStream);
//		});
//	}
//	asStream {
//		^Routine({ arg inval;
//			var count = 0, join, cond;
//			join = list.size;
//			cond = Condition({ count >= join });
//			list.do({ arg func; 
//				Routine({ arg time;
//					inval.
//					pattern.embedInStream(inval.copy);
//					count = count + 1;
//					cond.signal;
//				}).play;
//			});
//			cond.wait;
//		});
//	}
//}

Ppar : ListPattern {
	initStreams { arg priorityQ;
		list.do({ arg pattern, i; 
			priorityQ.put(0.0, pattern.asStream);
		});
	}
	asStream {
		var priorityQ, assn;
		
		priorityQ = PriorityQueue.new;
		
		^Routine.new({ arg inval;
		
			repeats.value.do({ arg j;
				var outval, stream, nexttime, now = 0.0;

				this.initStreams(priorityQ);
				
				// if first event not at time zero
				if (priorityQ.notEmpty and: { (nexttime = priorityQ.topPriority) > 0.0 }, {
					outval = inval;
					outval.put(\freq, \rest);					
					outval.put(\delta, nexttime);
					
					inval = outval.yield;
					now = nexttime;	
				});
				
				while({
					priorityQ.notEmpty
				},{
					stream = priorityQ.pop;
					outval = stream.next(inval);
					if (outval.isNil, {
						nexttime = priorityQ.topPriority;
						if (nexttime.notNil, {
							// that child stream ended, so rest until next one
							outval = inval;
							outval.put(\freq, \rest);					
							outval.put(\delta, nexttime - now);
							
							inval = outval.yield;
							now = nexttime;	
						},{
							priorityQ.clear;
						});		
					},{			
						// requeue stream
						priorityQ.put(now + outval.delta, stream);
						nexttime = priorityQ.topPriority;
						outval.put(\delta, nexttime - now);
						
						inval = outval.yield;
						now = nexttime;	
					});	
				});
			});
		});
	}
}	

Ptpar : Ppar {
	initStreams { arg priorityQ;
		forBy(0, list.size-1, 2, { arg i;
			priorityQ.put(list.at(i).value, list.at(i+1).asStream);
		});
	}
}

Pswitch : Pattern {
	var <>list, <>which=0;
	*new { arg list, which=0;
		^super.new.list_(list).which_(which)
	}
	asStream { 
		^Routine.new({ arg inval;
			var item, index, indexStream;
			
			indexStream = which.asStream;
			while ({
				(index = indexStream.next).notNil;
			},{
				inval = list.wrapAt(index).embedInStream(inval);
			});
		});
	}
	storeParamsOn { arg stream;
		stream << "(" <<<* [ list, which ] << ")";
	}
}

Pswitch1 : Pswitch {	
	asStream { 
		var streamList, indexStream;
		
		streamList = list.collect({ arg pattern; pattern.asStream; });
		indexStream = which.asStream;
		
		^FuncStream.new({ arg inval;
			var index;
			if ((index = indexStream.next).notNil, {
				streamList.wrapAt(index).next(inval);
			});
		},{
			streamList.do({ arg stream; stream.reset; });
		});
	}
}	

Ptuple : ListPattern {
	asStream { 
		^Routine.new({ arg inval;
			var item, streams, tuple, outval;
						
			streams = list.collect({ arg item; item.asStream });
			
			repeats.value.do({ arg j;
				var sawNil = false;
								
				while ({
					tuple = Array.new(streams.size);
					streams.do({ arg stream; 
						outval = stream.next(inval);
						if (outval.isNil, { sawNil = true; });
						tuple.add(outval);
					});
					sawNil.not
				},{
					inval = yield(tuple);
				});
					
			});
		});
	}
}

Place : Pseq {
	asStream { 
		^Routine.new({ arg inval;
			var item, offsetValue;
			
			offsetValue = offset.value;
			if (inval.eventAt('reverse') == true, {
				repeats.value.do({ arg j;
					list.size.reverseDo({ arg i;
						item = list @@ (i + offsetValue);
						if (item.isKindOf(SequenceableCollection), {
							item = item @@ j;
						});
						inval = item.embedInStream(inval);
					});
				});
			},{
				repeats.value.do({ arg j;
					list.size.do({ arg i;
						item = list @@ (i + offsetValue);
						if (item.isKindOf(SequenceableCollection), {
							item = item @@ j;
						});
						inval = item.embedInStream(inval);
					});
				});
			});
		});
	}
}

Pslide : ListPattern {
    // 'repeats' is the number of segments.
    // 'len' is the length of each segment.
    // 'step' is how far to step the start of each segment from previous.
    // 'start' is what index to start at.
    // indexing wraps around if goes past beginning or end.
    // step can be negative.

    var <>len, <>step, <>start;
    *new { arg list, repeats = 1, len = 3, step = 1, start = 0;
        ^super.new(list, repeats).len_(len).step_(step).start_(start)
    }
    asStream {
        ^Routine({ arg inval;
            var pos, item;
            pos = start;
            repeats.do({
                len.do({ arg j;
                    item = list.wrapAt(pos + j);
                    inval = item.embedInStream(inval);
                });
                pos = pos + step;
            });
        });
    }
}

