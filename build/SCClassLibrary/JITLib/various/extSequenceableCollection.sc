+SequenceableCollection {
	
	asArrayStream {
		var streamArray, array, size, res;
		res = FuncStream.new({ arg args;
			(size div: 2).do({ arg i;
				i = i*2+1;
				array[i] = streamArray[i].next(args)
			});
			array;
		}, {
			size = this.size;
			array = Array.newFrom(this);
			streamArray = array.collect({ arg item; item.asStream });
		});
		res.reset;
		^res
	}

}