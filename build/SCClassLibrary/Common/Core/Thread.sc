// you must not make any change at all to the order or number of 
// instance variables in these classes! 
// You should also not muck with the contents of the instance 
// variables unless you are sure you know what you are doing.
// You may add methods.

// Thread inherits from Stream for the benefit of its subclass Routine which can
// behave like a Stream. Thread itself is not used like a Stream.

Thread : Stream {
	var <state=0, func, stack, stackSize=0, method, block, frame, ip=0, sp=0;
	var top=0, numpop=0, returnLevels=0, receiver, numArgsPushed=0;
	var parent, terminalValue;
	var <primitiveError=0, <primitiveIndex=0, <randData=0;
	var <>time=0.0;

	*new { arg func, stackSize=512;
		^super.new.init(func, stackSize)
	}
	init { arg argFunc, argStackSize=512;
		_Thread_Init
		^this.primitiveFailed
	}
	
	randSeed_ { arg seed;
		// You supply an integer seed.
		// This method creates a new state vector and stores it in randData.
		// A state vector is an Int32Array of three 32 bit words.
		// SuperCollider uses the taus88 random number generator which has a 
		// period of 2**88, and passes all standard statistical tests.
		// Normally Threads inherit the randData state vector from the Thread that created it.
		_Thread_RandSeed
		^this.primitiveFailed;
	}
	failedPrimitiveName { _PrimName }
	
	// these make Thread act like an Object not like Stream.
	next { ^this }
	value { ^this }
	valueArray { ^this }

	*primitiveError { _PrimitiveError }
	*primitiveErrorString { _PrimitiveErrorString; }
}

Routine : Thread {
	
	// resume, next and value are synonyms
	resume { arg inval;
		_RoutineResume
		^this.primitiveFailed
	}
	next { arg inval;
		_RoutineResume
		^this.primitiveFailed
	}
	value { arg inval;
		_RoutineResume
		^this.primitiveFailed
	}
	valueArray { arg inval;
		^this.value(inval) 
	}
	
	reset { arg inval;
		_RoutineReset
		^this.primitiveFailed
	}
	stop { arg inval;
		_RoutineStop
		^this.primitiveFailed
	}
	
	// PRIVATE
	awake { arg inTime;
		time = inTime;
		^this.next(inTime)
	}
	prStart { arg inval;
		func.value(inval);
		
		// if the user's function returns then always yield nil
		nil.alwaysYield;
	}
}

Task : Routine {}

