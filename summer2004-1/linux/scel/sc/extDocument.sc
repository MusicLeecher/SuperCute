// copyright 2003 stefan kersten <steve@k-hornz.de>
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

+ Document {
	// Document class for Emacs interface.
	// Delegates to EmacsDocument.

	*initClass { }
	*open { | path, selectionStart = 0, selectionLength = 0, completionFunc |
		EmacsDocument.prNewFromPath(path, selectionStart, selectionLength, completionFunc);
		^nil
	}
	*new { | title = "Untitled", string = "", makeListener = false,  completionFunc |
		EmacsDocument.prNewFromString(title, string, makeListener, completionFunc);
		^nil
	}
	*listener { ^allDocuments.detectMsg(\isListener) }

	// PRIVATE
	*prBasicNew { ^super.new }
	*numberOfOpen { ^allDocuments.size }
	*newFromIndex { ^this.shouldNotImplement(thisMethod) }
	*prGetLast { ^allDocuments.last }
	*prGetIndexOfListener { ^this.shouldNotImplement(thisMethod) }
}

// EOF