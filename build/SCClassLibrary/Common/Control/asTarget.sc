
+Server {
	asTarget {
		^Group.basicNew(this, 1)
	}
}

+Node {
	asTarget {
		^this	
	}
}

+Nil {
	asTarget {
		^Group.basicNew(Server.default, 1)
	}
}