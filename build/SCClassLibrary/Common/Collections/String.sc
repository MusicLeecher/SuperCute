
String[char] : RawArray {
	
	asSymbol { 
		_StringAsSymbol 
		^this.primitiveFailed
	}
	asInteger { 
		_String_AsInteger
		^this.primitiveFailed
	}
	asFloat { 
		_String_AsFloat
		^this.primitiveFailed
	}
	
	compare { arg aString; _StringCompare }
	< { arg aString; ^this.compare(aString) < 0 }
	== { arg aString; ^this.compare(aString) == 0 }
	!= { arg aString; ^this.compare(aString) != 0 }
	hash { _StringHash }
	
	// no sense doing collect as per superclass collection
	performBinaryOpOnSimpleNumber { arg aSelector, aNumber; 
		^this.perform(aSelector, aNumber)
	}
	performBinaryOpOnComplex { arg aSelector, aComplex; 
		^this.perform(aSelector, aComplex)
	}
	performBinaryOpOnInfinitum { arg aSelector, aNumber; 
		^this.perform(aSelector, aNumber)
	}

	
	isString { ^true }
	asString { ^this }
	asCompileString { ^"\"" ++ this ++ "\"" }
	species { ^String }
	
	postln { _PostLine }
	post { _PostString }
	postcln { "// ".post; this.postln; }
	postc { "// ".post; this.post; }

	die { arg ... culprits; 
		"FATAL ERROR:\n".post;  
		this.postln;  
		culprits.do({ arg c; c.dump });
		this.halt; 
	}
	error { "ERROR:\n".post; this.postln; }
	warn { "WARNING:\n".post; this.postln }
	inform { ^this.postln }
	++ { arg anObject; ^this prCat: anObject.asString; }
	+ { arg anObject; ^this prCat: " " prCat: anObject.asString; }
	catArgs { arg ... items; ^this.catList(items) }
	scatArgs { arg ... items; ^this.scatList(items) }
	ccatArgs { arg ... items; ^this.scatList(items) }
	catList { arg list; 
		// concatenate this with a list as a string
		var string;
		string = this.copy;
		list.do({ arg item, i;
			string = string ++ item;
		});
		^string
	}
	scatList { arg list; 
		var string;
		string = this.copy;
		list.do({ arg item, i;
			string = string prCat: " " ++ item;
		});
		^string
	}
	ccatList { arg list; 
		var string;
		string = this.copy;
		list.do({ arg item, i;
			string = string prCat: ", " ++ item;
		});
		^string
	}
	split { arg separator=$/;
		var array,word;
		word="";
		array=[];
		separator=separator.ascii;
		
		this.do({arg let,i;
			if(let.ascii != separator ,{
				word=word++let;
			},{
				array=array.add(word);
				word="";
			});
		});
		^array.add(word);
	}
	
	containsStringAt { arg index, string;
		string.do({ arg char, i;
				if(char != this.at(index + i), { ^false })
		});
		^true
	}
	
	// case insensitive
	
	icontainsStringAt { arg index, string;
		string.do({ arg char, i;
				var myChar;
				myChar = this.at(index + i);
				if((char.toLower != myChar) and: {char.toUpper != myChar}, {  ^false  })
		})
		^true
	}
	
	contains { arg string;
		var firstChar;
		firstChar = string.at(0);
		this.do({	arg char,i;
			if(char == firstChar,{
				if(this.containsStringAt(i, string), { ^true });
			})
		});
		^false
	}
	
	// case insensitive
	containsi { arg string;
		var firstChar;
		firstChar = string.at(0);
		this.do({	arg char,i;
			if((char.toLower == firstChar) or: (char.toUpper == firstChar),{
				if(this.icontainsStringAt(i, string), { ^true });
			})
		});
		^false
	}
	

	escapeChar { arg charToEscape; // $"
		^this.class.streamContents({ arg st;
			this.do({ arg char;
				if(char == charToEscape,{ 
					st << $\\ 
				});
				st << char;
			})
		})
	}
	tr { arg from,to;
		^this.collect({ arg char;
			if(char == from,{to},{from})
		})
	}
	compile { ^thisProcess.interpreter.compile(this); }
	interpret { ^thisProcess.interpreter.interpret(this); } 
	interpretPrint { ^thisProcess.interpreter.interpretPrint(this); }
	
	*readNew { arg file;
		^file.readAllString;
	}
	prCat { arg aString; _ArrayCat }
	
	printOn { arg stream;
		stream.putAll(this);
	}
	storeOn { arg stream;
		stream.putAll(this.asCompileString);
	}
	
	inspectorClass { ^StringInspector }
	
	/// unix

	pathMatch { _StringPathMatch ^this.primitiveFailed } // glob
	loadPaths {
		var paths;
		paths = this.pathMatch;
		paths.do({ arg path;
			thisProcess.interpreter.executeFile(path);
		});
	}
	basename {
		_String_Basename;
		^this.primitiveFailed
	}
	dirname {
		_String_Dirname;
		^this.primitiveFailed
	}
	splitext {
		arg n, m;
		n = this.size;
		n.do({
			arg i;
			m = n - i - 1;

			if (this.at(m) == $\., {
				^[this.copyFromStart(m - 1), this.copyToEnd(m + 1)]
			});

			if (this.at(m) == $/, {
				^[this.copy, nil]
			});
		});
		^[this.copy, nil]
	}
	
	// runs a unix command and returns the result code.
	//unixCmd { _String_System ^this.primitiveFailed }
	
	// runs a unix command and sends stdout to the post window
	unixCmd { _String_POpen ^this.primitiveFailed }

	gethostbyname { _GetHostByName ^this.primitiveFailed }
	
	/// code gen
	codegen_UGenCtorArg { arg stream; 
		stream << this.asCompileString; 
	}
	ugenCodeString { arg ugenIndex, isDecl, inputNames=#[], inputStrings=#[];
		_UGenCodeString
		^this.primitiveFailed
	}
	
}

