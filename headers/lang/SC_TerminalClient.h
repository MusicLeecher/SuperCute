// emacs:		-*- c++ -*-
// file:		SC_TerminalClient.h
// copyright:	2003 stefan kersten <steve@k-hornz.de>
// cvs:			$Id$

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

#ifndef SC_TERMINALCLIENT_H_INCLUDED
#define SC_TERMINALCLIENT_H_INCLUDED

#include "SC_LanguageClient.h"
#include <pthread.h>

// =====================================================================
// SC_TerminalClient - command line sclang client.
// =====================================================================

class SC_TerminalClient : public SC_LanguageClient
{
public:
	SC_TerminalClient();

	void run(int argc, char** argv);

	void post(const char *fmt, va_list ap, bool error);
	void post(char c);
	void post(const char* str, size_t len);
	void flush();

protected:
	void printUsage(FILE* file);
	void printOptions(FILE* file);

	int readCommands(FILE* inputFile);
	static void* tickThreadFunc(void* data);

private:
	int				mArgc;
	char**			mArgv;
	bool			mShouldBeRunning;
	pthread_t		mTickThread;
};

#endif // SC_TERMINALCLIENT_H_INCLUDED
