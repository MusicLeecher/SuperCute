MultiLevelIdentityDictionary : Collection 
{
	
	var <>dictionary;

	*new {
		^super.new.init
	}
	init {
		dictionary = this.newInternalNode;
	}
	
	newInternalNode { ^this.nodeType.new }

	nodeType {
		^IdentityDictionary;
	}

	at { arg ... path;
		^this.atPath(path)
	}
	atPath {
		arg path;
		var item;
		item = dictionary;
		path.do({ arg name; 
			item = item.at(name);
			if (item.isNil, { ^nil });
		});
		^item
	}

	put { arg ... path;
		var item;
		item = path.pop;
		^this.putAtPath(path, item);
	}	
	putAtPath { arg path, val;
		var item, lastName;
		path = path.copy;
		lastName = path.pop;
		item = dictionary;
		path.do({ arg name; 
			item = item.atFail(name, {
				var newitem; 
				newitem = this.newInternalNode;
				item.put(name, newitem);
				newitem
			});
		});
		item.put(lastName, val);
	}

	create { arg ... args;
		var item;
		item = dictionary;
		args.do({ arg name; 
			item = item.atFail(name, {
				var newitem; 
				newitem = this.newInternalNode;
				item.put(name, newitem);
				newitem
			});
		});
	}	
	
	choose { arg ... start;
		var item;
		if(start.isEmpty,{
			item = dictionary;
		},{
			item = this.performList(\at,start);
			if(item.isNil,{
				("Library-choose start address not found:" + start).die;
			});
		});
		^this.prChooseFrom(item);
	}
	putTree { arg ... items;
		this.prPutTree([],items)
	}
	postTree { arg obj,tabs=0;
		if(obj.isNil,{ obj = dictionary });
		if(obj.isKindOf(this.nodeType),{
			"".postln;
			obj.keysValuesDo({ arg k,v;
				tabs.do({ Char.tab.post });
				k.post;
				": ".post;
				this.postTree(v,tabs + 1)
			});
		},{
			Char.tab.post;
			obj.asString.postln;
		})
	}
	do { arg function;
		dictionary.do(function);
	}
	removeAt {
		arg ... path;
		this.removeAtPath(path)
	}
	removeAtPath { arg path;
		var item, lastName;
		path = path.copy;
		lastName = path.pop;
		item = dictionary;
		path.do({ arg name; 
			item = item.at(name); 
			if (item.isNil, { ^nil });
		});
		item.removeAt(lastName);
	}

	
	//private
	add { arg assn;
		this.put(assn.key, assn.value);
	}
	remove { ^this.shouldNotImplement(thisMethod) }
	removeFail { ^this.shouldNotImplement(thisMethod) }
	
	prChooseFrom { arg dict;
		var item;
		item = dict.choose;
		if(item.isKindOf(this.nodeType),{
			^this.prChooseFrom(item);
		},{
			^item
		})
	}
	prPutTree { arg keys,items;
		forBy(0,items.size - 1,2,{ arg i;
			var key,item;
			key = items.at(i);
			item = items.at(i + 1);
			if(item.isKindOf(Function),{
				this.performList(\put,keys ++ [key,item]);
			},{
				//array
				this.prPutTree(keys ++ [key],item);
			})
		})
	}
	leaves { arg startAt;
		if(startAt.isNil,{
			startAt = dictionary;
		},{
			startAt = this.performList(\at,startAt);
		});
		^this.prNestedValuesFromDict(startAt);
	}
	prNestedValuesFromDict { arg dict;
		^dict.values.collect({ arg thing;
			if(thing.isKindOf(this.nodeType),{
				this.prNestedValuesFromDict(thing)
			},{
				thing
			})
		})
	}

	// Tree-like do methods
	leafDo {
		arg func;

		this.dictionary.keysValuesDo({
			arg name, object;
			this.doLeafDo([name], object, func)
		})
	}
	doLeafDo {
		arg path, object, func;

		if (object.isKindOf(this.nodeType), {
			object.keysValuesDo({
				arg name, subobject;
				this.doLeafDo(path ++ [name], subobject, func)
			});
		}, {
			func.value(path, object);
		})
	}
	treeDo {
		arg branchFunc, leafFunc, argument0, postBranchFunc;
		var result;

		result = this.doTreeDo([], this.dictionary, branchFunc, leafFunc, argument0, postBranchFunc);
		^result;
	}
	doTreeDo {
		arg path, object, branchFunc, leafFunc, argument, postBranchFunc;
		var result;

		if (object.isKindOf(this.nodeType), {
			if (branchFunc.notNil, {
				result = branchFunc.value(path, argument, object);
			}, {
				result = argument;
			});
			object.keysValuesDo({
				arg name, subobject;
				this.doTreeDo(path ++ [name], subobject, branchFunc, leafFunc, result, postBranchFunc)
			});
			if (postBranchFunc.notNil, {
				postBranchFunc.value(path, result);
			});
			^result
		}, {
			leafFunc.value(path, object, argument);
		})
	}
}



Library : MultiLevelIdentityDictionary 
{
	classvar <global;
	
	*initClass {
		global = this.new;
	}

	*clear {
		global = this.new;
	}
	*at { arg ... args;
		^global.performList(\at, args);
	}

	*atList { arg args;
		^global.performList(\at,args)
	}
	*putList { arg args;
		^global.performList(\put,args)
	}

	*put { arg ... args;
		global.performList(\put,args);
	}
	*create { arg ... args;
		^global.performList(\create, args);
	}

	*postTree {
		global.postTree
	}

}



