/*
	SuperCollider real time audio synthesis system
    Copyright (c) 2002 James McCartney. All rights reserved.
	http://www.audiosynth.com

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/*
 * Having SequencedCommands allows performing actions that might otherwise require
 * taking a mutex, which is undesirable in a real time thread. 
 * Some commands require several stages of processing at both the real time
 * and non real time levels. This class does the messaging between levels for you
 * so that you only need to write the functions.
 */

#ifndef _SC_SequencedCommand_
#define _SC_SequencedCommand_

#include "OSC_Packet.h"
#include "SC_World.h"
#include "SC_BufGen.h"
#include "sc_msg_iter.h"
#include <sndfile.h>
#include <new>

#define CallSequencedCommand(T, inWorld, inSize, inData, inReply)	\
	void* space = World_Alloc(inWorld, sizeof(T)); \
	T *cmd = new (space) T(inWorld, inReply); \
	if (!cmd) return kSCErr_Failed; \
	int err = cmd->Init(inData, inSize); \
	if (err) { \
		cmd->~T(); \
		World_Free(inWorld, space); \
		return err; \
	} \
	if (inWorld->mRealTime) cmd->CallNextStage(); \
	else cmd->CallEveryStage();
	

class SC_SequencedCommand
{
public:
	SC_SequencedCommand(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~SC_SequencedCommand();
	
	void Delete();
		
	void CallEveryStage();
	void CallNextStage();
		
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage1();	//     real time
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
	void SendDone(char *inCommandName);
	
protected:
	int mNextStage;
	ReplyAddress mReplyAddress;
	World *mWorld;
	
	int mMsgSize;
	char *mMsgData;
	
	virtual void CallDestructor()=0;
};

///////////////////////////////////////////////////////////////////////////

class BufGenCmd : public SC_SequencedCommand
{
public:
	BufGenCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~BufGenCmd();
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	BufGen *mBufGen;
	sc_msg_iter mMsg;
	char *mData;
	int mSize;
	
	virtual void CallDestructor();
	
};

///////////////////////////////////////////////////////////////////////////

class BufAllocCmd : public SC_SequencedCommand
{
public:
	BufAllocCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	SndBuf mSndBuf;
	int mNumChannels, mNumFrames;
	float *mFreeData;
	
	virtual void CallDestructor();
	
};

///////////////////////////////////////////////////////////////////////////

class BufShmAllocCmd : public SC_SequencedCommand
{
public:
	BufShmAllocCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	SndBuf mSndBuf;
	int mNumChannels, mNumFrames;
	float *mFreeData;
	key_t mID;
	
	virtual void CallDestructor();
	
};

///////////////////////////////////////////////////////////////////////////


class BufFreeCmd : public SC_SequencedCommand
{
public:
	BufFreeCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	float *mFreeData;
	
	virtual void CallDestructor();
};


///////////////////////////////////////////////////////////////////////////


class BufCloseCmd : public SC_SequencedCommand
{
public:
	BufCloseCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	
	virtual void CallDestructor();
};


///////////////////////////////////////////////////////////////////////////


class BufZeroCmd : public SC_SequencedCommand
{
public:
	BufZeroCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class BufAllocReadCmd : public SC_SequencedCommand
{
public:
	BufAllocReadCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~BufAllocReadCmd();
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	float *mFreeData;
	SndBuf mSndBuf;
	char *mFilename;
	int mFileOffset, mNumFrames;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class BufReadCmd : public SC_SequencedCommand
{
public:
	BufReadCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~BufReadCmd();
	
	virtual int Init(char *inData, int inSize);

	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	char *mFilename;
	int mFileOffset, mNumFrames, mBufOffset;
	bool mLeaveFileOpen;
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class BufWriteCmd : public SC_SequencedCommand
{
public:
	BufWriteCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~BufWriteCmd();
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	int mBufIndex;
	char *mFilename;
	SF_INFO mFileInfo;
	int mNumFrames, mBufOffset;
	bool mLeaveFileOpen;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class AudioQuitCmd : public SC_SequencedCommand
{
public:
	AudioQuitCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class AudioStatusCmd : public SC_SequencedCommand
{
public:
	AudioStatusCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual bool Stage2();	// non real time
	
protected:
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class NotifyCmd : public SC_SequencedCommand
{
public:
	NotifyCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	
protected:
	
	virtual void CallDestructor();
	
	int mOnOff;
	int mID;
};


///////////////////////////////////////////////////////////////////////////

#define CallSendFailureCommand(inWorld, inCmdName, inErrString, inReply)	\
	void* space = World_Alloc(inWorld, sizeof(SendFailureCmd)); \
	SendFailureCmd *cmd = new (space) SendFailureCmd(inWorld, inReply); \
	if (!cmd) return kSCErr_Failed; \
	cmd->InitSendFailureCmd(inCmdName, inErrString); \
	if (inWorld->mRealTime) cmd->CallNextStage(); \
	else cmd->CallEveryStage(); \

class SendFailureCmd : public SC_SequencedCommand
{
public:
	SendFailureCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~SendFailureCmd();

	virtual void InitSendFailureCmd(const char *inCmdName, const char* inErrString);
	
	virtual bool Stage2();	// non real time
	
protected:
	char *mCmdName, *mErrString;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

#include "SC_GraphDef.h"

class LoadSynthDefCmd : public SC_SequencedCommand
{
public:
	LoadSynthDefCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~LoadSynthDefCmd();
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	char *mFilename;
	GraphDef *mDefs;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

#include "SC_GraphDef.h"

class RecvSynthDefCmd : public SC_SequencedCommand
{
public:
	RecvSynthDefCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~RecvSynthDefCmd();
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	char *mBuffer;
	GraphDef *mDefs;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class LoadSynthDefDirCmd : public SC_SequencedCommand
{
public:
	LoadSynthDefDirCmd(World *inWorld, ReplyAddress *inReplyAddress);
	virtual ~LoadSynthDefDirCmd();
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	virtual bool Stage3();	//     real time
	virtual void Stage4();	// non real time
	
protected:
	char *mFilename;
	GraphDef *mDefs;
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

class SendReplyCmd : public SC_SequencedCommand
{
public:
	SendReplyCmd(World *inWorld, ReplyAddress *inReplyAddress);
	
	virtual int Init(char *inData, int inSize);
	
	virtual bool Stage2();	// non real time
	
protected:
	
	virtual void CallDestructor();
};

///////////////////////////////////////////////////////////////////////////

#endif

