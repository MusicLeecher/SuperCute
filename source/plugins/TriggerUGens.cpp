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


#include "SC_PlugIn.h"

static InterfaceTable *ft;

//////////////////////////////////////////////////////////////////////////////////////////////////

struct Trig1 : public Unit
{
	float m_prevtrig;
	long mCounter;
};

struct Trig : public Unit
{
	float mLevel;
	float m_prevtrig;
	long mCounter;
};

struct SendTrig : public Unit
{
	float m_prevtrig;
};

struct ToggleFF : public Unit
{
	float mLevel;
	float m_prevtrig;
};

struct SetResetFF : public Unit
{
	float mLevel;
	float m_prevtrig, m_prevreset;
};

struct Latch : public Unit
{
	float mLevel;
	float m_prevtrig;
};

struct Gate : public Unit
{
	float mLevel;
	float m_prevtrig;
};

struct Schmidt : public Unit
{
	float mLevel;
};

struct PulseDivider : public Unit
{
	float mLevel;
	float m_prevtrig;
	long mCounter;
};

struct PulseCount : public Unit
{
	float mLevel;
	float m_prevtrig, m_prevreset;
};

struct Ramp : public Unit
{
	double mLevel;
	float m_prevtrig;
	// slope, trig, start
};

struct TDelay : public Unit
{
	long mCounter;
	float m_prevtrig;
};

struct ZeroCrossing : public Unit
{
	float mLevel, m_prevfrac, m_previn;
	int32 mCounter;
};

struct Timer : public Unit
{
	float mLevel, m_prevfrac, m_previn;
	int32 mCounter;
};

struct Sweep : public Unit
{
	float mLevel, m_previn;
};

struct Phasor : public Unit
{
	float mLevel, m_previn;
};

struct Peak : public Unit
{
	float mLevel;
	float m_prevtrig;
};

struct MostChange : public Unit
{
	float mPrevA, mPrevB;
	int mRecent;
};

struct LeastChange : public Unit
{
	float mPrevA, mPrevB;
	int mRecent;
};

struct LastValue : public Unit
{
	float mPrev;
	float mCurr;
};

struct Done : public Unit
{
	Unit *m_src;
};

struct FreeSelf : public Unit
{
};

struct PauseSelf : public Unit
{
	float m_prevtrig;
};

struct Pause : public Unit
{
	int m_state;
};

struct Free : public Unit
{
	int m_state;
};

struct FreeSelfWhenDone : public Unit
{
	Unit *m_src;
};

struct PauseSelfWhenDone : public Unit
{
	Unit *m_src;
};


extern "C"
{
	void load(InterfaceTable *inTable);

void Trig1_Ctor(Trig1 *unit);
void Trig1_next(Trig1 *unit, int inNumSamples);
void Trig1_next_k(Trig1 *unit, int inNumSamples);

void Trig_Ctor(Trig *unit);
void Trig_next(Trig *unit, int inNumSamples);
void Trig_next_k(Trig *unit, int inNumSamples);

void SendTrig_Ctor(SendTrig *unit);
void SendTrig_next(SendTrig *unit, int inNumSamples);
void SendTrig_next_aka(SendTrig *unit, int inNumSamples);

void SetResetFF_Ctor(SetResetFF *unit);
void SetResetFF_next(SetResetFF *unit, int inNumSamples);

void ToggleFF_Ctor(ToggleFF *unit);
void ToggleFF_next(ToggleFF *unit, int inNumSamples);

void Latch_Ctor(Latch *unit);
void Latch_next_ak(Latch *unit, int inNumSamples);
void Latch_next_aa(Latch *unit, int inNumSamples);

void Gate_Ctor(Gate *unit);
void Gate_next_ak(Gate *unit, int inNumSamples);
void Gate_next_aa(Gate *unit, int inNumSamples);

void Schmidt_Ctor(Schmidt *unit);
void Schmidt_next(Schmidt *unit, int inNumSamples);

void PulseDivider_Ctor(PulseDivider *unit);
void PulseDivider_next(PulseDivider *unit, int inNumSamples);

void PulseCount_Ctor(PulseCount *unit);
void PulseCount_next(PulseCount *unit, int inNumSamples);

void TDelay_Ctor(TDelay *unit);
void TDelay_next(TDelay *unit, int inNumSamples);

void ZeroCrossing_Ctor(ZeroCrossing *unit);
void ZeroCrossing_next_a(ZeroCrossing *unit, int inNumSamples);

void Timer_Ctor(Timer *unit);
void Timer_next_a(Timer *unit, int inNumSamples);

void Sweep_Ctor(Sweep *unit);
void Sweep_next_0k(Sweep *unit, int inNumSamples);
void Sweep_next_0a(Sweep *unit, int inNumSamples);
void Sweep_next_kk(Sweep *unit, int inNumSamples);
void Sweep_next_ka(Sweep *unit, int inNumSamples);
void Sweep_next_ak(Sweep *unit, int inNumSamples);
void Sweep_next_aa(Sweep *unit, int inNumSamples);

void Phasor_Ctor(Phasor *unit);
void Phasor_next_kk(Phasor *unit, int inNumSamples);
void Phasor_next_ak(Phasor *unit, int inNumSamples);
void Phasor_next_aa(Phasor *unit, int inNumSamples);

void Peak_Ctor(Peak *unit);
void Peak_next_ak(Peak *unit, int inNumSamples);
void Peak_next_ai(Peak *unit, int inNumSamples);
void Peak_next_aa(Peak *unit, int inNumSamples);

void MostChange_Ctor(MostChange *unit);
void MostChange_next_ak(MostChange *unit, int inNumSamples);
void MostChange_next_ka(MostChange *unit, int inNumSamples);
void MostChange_next_aa(MostChange *unit, int inNumSamples);

void LeastChange_Ctor(LeastChange *unit);
void LeastChange_next_ak(LeastChange *unit, int inNumSamples);
void LeastChange_next_ka(LeastChange *unit, int inNumSamples);
void LeastChange_next_aa(LeastChange *unit, int inNumSamples);

void LastValue_Ctor(LastValue *unit);
void LastValue_next_ak(LastValue *unit, int inNumSamples);
void LastValue_next_kk(LastValue *unit, int inNumSamples);

void Done_Ctor(Done *unit);
void Done_next(Done *unit, int inNumSamples);

void FreeSelf_Ctor(FreeSelf *unit);
void FreeSelf_next(FreeSelf *unit, int inNumSamples);

void FreeSelfWhenDone_Ctor(FreeSelfWhenDone *unit);
void FreeSelfWhenDone_next(FreeSelfWhenDone *unit, int inNumSamples);

void PauseSelf_Ctor(PauseSelf *unit);
void PauseSelf_next(PauseSelf *unit, int inNumSamples);

void Pause_Ctor(Pause *unit);
void Pause_next(Pause *unit, int inNumSamples);

void Free_Ctor(Free *unit);
void Free_next(Free *unit, int inNumSamples);

void PauseSelfWhenDone_Ctor(PauseSelfWhenDone *unit);
void PauseSelfWhenDone_next(PauseSelfWhenDone *unit, int inNumSamples);

}

////////////////////////////////////////////////////////////////////////////////////////////////////////


void Trig1_Ctor(Trig1 *unit)
{
	if (unit->mCalcRate == calc_FullRate && INRATE(0) != calc_FullRate) {
		SETCALC(Trig1_next_k);
	} else {
		SETCALC(Trig1_next);
	}
	unit->mCounter = 0;
	unit->m_prevtrig = 0.f;
	
	ZOUT0(0) = 0.f;
}

void Trig1_next(Trig1 *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float dur = ZIN0(1);
	float sr = unit->mRate->mSampleRate;
	float prevtrig = unit->m_prevtrig;
	unsigned long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		float zout;
		if (counter > 0) { 
			zout = --counter ? 1.f : 0.f;
		} else {
			if (curtrig > 0.f && prevtrig <= 0.f) {
				counter = (long)(dur * sr + .5f);
				if (counter < 1) counter = 1;
				zout = 1.f;
			} else {
				zout = 0.f;
			}
		}
		prevtrig = curtrig;
		ZXP(out) = zout;
	);
	unit->m_prevtrig = prevtrig;
	unit->mCounter = counter;
}

void Trig1_next_k(Trig1 *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float curtrig = ZIN0(0);
	float dur = ZIN0(1);
	float sr = unit->mRate->mSampleRate;
	float prevtrig = unit->m_prevtrig;
	unsigned long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float zout;
		if (counter > 0) { 
			zout = --counter ? 1.f : 0.f;
		} else {
			if (curtrig > 0.f && prevtrig <= 0.f) {
				counter = (long)(dur * sr + .5f);
				if (counter < 1) counter = 1;
				zout = 1.f;
			} else {
				zout = 0.f;
			}
		}
		prevtrig = curtrig;
		ZXP(out) = zout;
	);
	unit->m_prevtrig = prevtrig;
	unit->mCounter = counter;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////

void Trig_Ctor(Trig *unit)
{
	if (unit->mCalcRate == calc_FullRate && INRATE(0) != calc_FullRate) {
		SETCALC(Trig_next_k);
	} else {
		SETCALC(Trig_next);
	}

	unit->mCounter = 0;
	unit->m_prevtrig = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}

void Trig_next(Trig *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float dur = ZIN0(1);
	float sr = unit->mRate->mSampleRate;
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;
	unsigned long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		float zout;
		if (counter > 0) { 
			zout = --counter ? level : 0.f;
		} else {
			if (curtrig > 0.f && prevtrig <= 0.f) {
				counter = (long)(dur * sr + .5f);
				if (counter < 1) counter = 1;
				level = curtrig;
				zout = level;
			} else {
				zout = 0.f;
			}
		}
		prevtrig = curtrig;
		ZXP(out) = zout;
	);
	unit->m_prevtrig = prevtrig;
	unit->mCounter = counter;
	unit->mLevel = level;
}

void Trig_next_k(Trig *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float curtrig = ZIN0(0);
	float dur = ZIN0(1);
	float sr = unit->mRate->mSampleRate;
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;
	unsigned long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float zout;
		if (counter > 0) { 
			zout = --counter ? level : 0.f;
		} else {
			if (curtrig > 0.f && prevtrig <= 0.f) {
				counter = (long)(dur * sr + .5f);
				if (counter < 1) counter = 1;
				level = curtrig;
				zout = level;
			} else {
				zout = 0.f;
			}
		}
		prevtrig = curtrig;
		ZXP(out) = zout;
	);
	unit->m_prevtrig = prevtrig;
	unit->mCounter = counter;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void SendTrig_Ctor(SendTrig *unit)
{
	if (INRATE(2) == calc_FullRate) {
		SETCALC(SendTrig_next_aka);
	} else {
		SETCALC(SendTrig_next);
	}
	unit->m_prevtrig = 0.f;
}

void SendTrig_next(SendTrig *unit, int inNumSamples)
{
	float *trig = ZIN(0);
	float prevtrig = unit->m_prevtrig;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		if (curtrig > 0.f && prevtrig <= 0.f) {
			SendTrigger(&unit->mParent->mNode, (int)ZIN0(1), ZIN0(2));
		}
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
}

void SendTrig_next_aka(SendTrig *unit, int inNumSamples)
{
	float *trig = ZIN(0);
        float *value = ZIN(2);
	float prevtrig = unit->m_prevtrig;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
                float curval = ZXP(value);
		if (curtrig > 0.f && prevtrig <= 0.f) {
			SendTrigger(&unit->mParent->mNode, (int)ZIN0(1), curval);
		}
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
}



////////////////////////////////////////////////////////////////////////////////////////////////////////

void SetResetFF_Ctor(SetResetFF *unit)
{
	SETCALC(SetResetFF_next);
	unit->m_prevtrig = 0.f;
	unit->m_prevreset = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}


void SetResetFF_next(SetResetFF *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float *reset = ZIN(1);
	float prevtrig = unit->m_prevtrig;
	float prevreset = unit->m_prevreset;
	float level = unit->mLevel;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		float curreset = ZXP(reset);
		if (prevreset <= 0.f && curreset > 0.f) level = 0.f;
		else if (prevtrig <= 0.f && curtrig > 0.f) level = 1.f;
		ZXP(out) = level;
		prevtrig = curtrig;
		prevreset = curreset;
	);
	unit->m_prevtrig = prevtrig;
	unit->m_prevreset = prevreset;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////


void ToggleFF_Ctor(ToggleFF *unit)
{
	SETCALC(ToggleFF_next);

	unit->m_prevtrig = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}


void ToggleFF_next(ToggleFF *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		if (prevtrig <= 0.f && curtrig > 0.f) level = 1.f - level;
		ZXP(out) = level;
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void Latch_Ctor(Latch *unit)
{
	if (INRATE(1) == calc_FullRate) {
		SETCALC(Latch_next_aa);
	} else {
		SETCALC(Latch_next_ak);
	}

	unit->m_prevtrig = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}


void Latch_next_ak(Latch *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float level = unit->mLevel;
	
	float curtrig = ZIN0(1);
	if (unit->m_prevtrig <= 0.f && curtrig > 0.f) level = ZIN0(0);
	
	LOOP(inNumSamples, ZXP(out) = level; );

	unit->m_prevtrig = curtrig;
	unit->mLevel = level;
}


void Latch_next_aa(Latch *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float *trig = ZIN(1);
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		if (prevtrig <= 0.f && curtrig > 0.f) level = ZXP(in);
		else { PZ(in); }
		ZXP(out) = level;
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
	unit->mLevel = level;
}



////////////////////////////////////////////////////////////////////////////////////////////////////////


void Gate_Ctor(Gate *unit)
{
	if (INRATE(1) == calc_FullRate) {
		SETCALC(Gate_next_aa);
	} else {
		SETCALC(Gate_next_ak);
	}

	unit->m_prevtrig = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}



void Gate_next_ak(Gate *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float *trig = ZIN(1);
	float level = unit->mLevel;
	
	float curtrig = ZXP(trig);
	if (curtrig > 0.f) {
		LOOP(inNumSamples, 
			level = ZXP(in);
			ZXP(out) = level;
		);
		unit->mLevel = level;
	} else {
		LOOP(inNumSamples, 
			ZXP(out) = level;
		);
	}
	unit->m_prevtrig = curtrig;
}



void Gate_next_aa(Gate *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float *trig = ZIN(1);
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		if (curtrig > 0.f) level = ZXP(in);
		else { PZ(in); }
		ZXP(out) = level;
	);
	unit->mLevel = level;
	unit->m_prevtrig = prevtrig;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////


void Schmidt_Ctor(Schmidt *unit)
{
	SETCALC(Schmidt_next);

	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}

void Schmidt_next(Schmidt *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float lo = ZIN0(1);
	float hi = ZIN0(2);
	float level = unit->mLevel;
	LOOP(inNumSamples, 
		float zin = ZXP(in);
		if (level == 1.) {
			if (zin < lo) level = 0.f;
		} else {
			if (zin > hi) level = 1.f;
		}
		ZXP(out) = level;
	);
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void PulseDivider_Ctor(PulseDivider *unit)
{
	SETCALC(PulseDivider_next);

	unit->m_prevtrig = 0.f;
	unit->mLevel = 0.f;
	unit->mCounter = (long)floor(ZIN0(2) + 0.5);
	
	ZOUT0(0) = 0.f;
}


void PulseDivider_next(PulseDivider *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	long div = (long)ZIN0(1);
	float prevtrig = unit->m_prevtrig;
	long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float z;
		float curtrig = ZXP(trig);
		if (prevtrig <= 0.f && curtrig > 0.f) {
			counter++;
			if (counter >= div) {
				counter = 0;
				z = 1.f;
			} else {
				z = 0.f;
			}
		} else {
			z = 0.f;
		}
		ZXP(out) = z;
		prevtrig = curtrig;
	);
	unit->mCounter = counter;
	unit->m_prevtrig = prevtrig;
}

//////////////////////////////////////////////////////////////////////////////////////////


void PulseCount_Ctor(PulseCount *unit)
{
	SETCALC(PulseCount_next);

	unit->m_prevtrig = 0.f;
	unit->m_prevreset = 0.f;
	unit->mLevel = 0.f;
	
	ZOUT0(0) = 0.f;
}


void PulseCount_next(PulseCount *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float *reset = ZIN(1);
	float prevtrig = unit->m_prevtrig;
	float prevreset = unit->m_prevreset;
	float level = unit->mLevel;

	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		float curreset = ZXP(reset);
		if (prevreset <= 0.f && curreset > 0.f) level = 0.f;
		else if (prevtrig <= 0.f && curtrig > 0.f) level += 1.f;
		ZXP(out) = level;
		prevtrig = curtrig;
		prevreset = curreset;
	);
	unit->mLevel = level;
	unit->m_prevtrig = prevtrig;
	unit->m_prevreset = prevreset;
}

//////////////////////////////////////////////////////////////////////////////////////////

void TDelay_Ctor(TDelay *unit)
{
	SETCALC(TDelay_next);

	unit->m_prevtrig = 0.f;
	unit->mCounter = 0;
	
	ZOUT0(0) = 0.f;
}



void TDelay_next(TDelay *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *trig = ZIN(0);
	float dur = ZIN0(1);
	float prevtrig = unit->m_prevtrig;
	long counter = unit->mCounter;
	
	LOOP(inNumSamples, 
		float curtrig = ZXP(trig);
		float zout;
		if (counter > 1) {
			counter--;
			zout = 0.f;
		} else if (counter<=0) {
			if (prevtrig <= 0.f && curtrig > 0.f) {
				counter = (long)(dur * unit->mRate->mSampleRate + .5f);
				if (counter < 1) counter = 1;
			}
			zout = 0.f;
		} else {
			counter = 0;
			zout = 1.f;
		}
		ZXP(out) = zout;
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
	unit->mCounter = counter;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////


void ZeroCrossing_Ctor(ZeroCrossing *unit)
{
	SETCALC(ZeroCrossing_next_a);

	unit->m_prevfrac = 0.f;
	unit->m_previn = ZIN0(0);
	ZOUT0(0) = unit->mLevel = 0.f;
	unit->mCounter = 0;
}

void ZeroCrossing_next_a(ZeroCrossing *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float previn = unit->m_previn;
	float prevfrac = unit->m_prevfrac;
	float level = unit->mLevel;
	long counter = unit->mCounter;
	
	LOOP(inNumSamples,
		counter++;
		float curin = ZXP(in);
		if (counter > 4 && previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = unit->mRate->mSampleRate / (frac + counter - prevfrac);
			prevfrac = frac;
			counter = 0;
		}
		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->m_prevfrac = prevfrac;
	unit->mLevel = level;
	unit->mCounter = counter;	
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void Timer_Ctor(Timer *unit)
{
	SETCALC(Timer_next_a);

	unit->m_prevfrac = 0.f;
	unit->m_previn = ZIN0(0);
	ZOUT0(0) = unit->mLevel = 0.f;
	unit->mCounter = 0;
}

void Timer_next_a(Timer *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float previn = unit->m_previn;
	float prevfrac = unit->m_prevfrac;
	float level = unit->mLevel;
	long counter = unit->mCounter;
	
	LOOP(inNumSamples,
		counter++;
		float curin = ZXP(in);
		if (previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = unit->mRate->mSampleDur * (frac + counter - prevfrac);
			prevfrac = frac;
			counter = 0;
		}
		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->m_prevfrac = prevfrac;
	unit->mLevel = level;
	unit->mCounter = counter;	
}


////////////////////////////////////////////////////////////////////////////////////////////////////////

void Sweep_Ctor(Sweep *unit)
{
	if (INRATE(0) == calc_ScalarRate) {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(Sweep_next_0a);
		} else {
			SETCALC(Sweep_next_0k);
		}
	} else if (INRATE(0) == calc_BufRate) {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(Sweep_next_ka);
		} else {
			SETCALC(Sweep_next_kk);
		}
	} else {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(Sweep_next_aa);
		} else {
			SETCALC(Sweep_next_ak);
		}
	}

	unit->m_previn = ZIN0(0);
	ZOUT0(0) = unit->mLevel = 0.f;
}

// this is a test
// this is another test

void Sweep_next_0k(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float rate = ZIN0(1) * SAMPLEDUR;
	float level = unit->mLevel;
	
	LOOP(inNumSamples,
		level += rate;
		ZXP(out) = level;
	);
	
	unit->mLevel = level;
}

void Sweep_next_0a(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *rate = ZIN(1);
	float level = unit->mLevel;
	float sampledur = SAMPLEDUR;
	
	LOOP(inNumSamples,
		float zrate = ZXP(rate) * sampledur;
		level += zrate;
		ZXP(out) = level;
	);
	
	unit->mLevel = level;
}

void Sweep_next_kk(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float curin = ZIN0(0);
	float rate = ZIN0(1);
	float previn = unit->m_previn;
	float level = unit->mLevel;

	if (previn <= 0.f && curin > 0.f) {
		float frac = -previn/(curin-previn);
		level = frac * rate;
	}
	
	LOOP(inNumSamples,
		level += rate;
		ZXP(out) = level;
	);
	
	unit->m_previn = curin;
	unit->mLevel = level;
}

void Sweep_next_ka(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float curin = ZIN0(0);
	float *rate = ZIN(1);
	float previn = unit->m_previn;
	float level = unit->mLevel;
	float sampledur = SAMPLEDUR;

	if (previn <= 0.f && curin > 0.f) {
		float frac = -previn/(curin-previn);
		level = frac * rate[ZOFF] * sampledur;
	}
	
	LOOP(inNumSamples,
		float zrate = ZXP(rate) * sampledur;
		level += zrate;
		ZXP(out) = level;
	);
	
	unit->m_previn = curin;
	unit->mLevel = level;
}

void Sweep_next_ak(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float rate = ZIN0(1) * SAMPLEDUR;
	float previn = unit->m_previn;
	float level = unit->mLevel;
	
	LOOP(inNumSamples,
		float curin = ZXP(in);
		if (previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = frac * rate;
		} else {
			level += rate;
		}
		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->mLevel = level;
}

void Sweep_next_aa(Sweep *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float *rate = ZIN(1);
	float previn = unit->m_previn;
	float level = unit->mLevel;
	float sampledur = SAMPLEDUR;
	
	LOOP(inNumSamples,
		float curin = ZXP(in);
		float zrate = ZXP(rate) * sampledur;
		if (previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = frac * zrate;
		} else {
			level += zrate;
		}
		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void Phasor_Ctor(Phasor *unit)
{
	if (unit->mCalcRate == calc_FullRate) {
		if (INRATE(0) == calc_FullRate) {
			if (INRATE(1) == calc_FullRate) {
				SETCALC(Phasor_next_aa);
			} else {
				SETCALC(Phasor_next_ak);
			}
		} else {
			SETCALC(Phasor_next_kk);
		}
	} else {
		SETCALC(Phasor_next_ak);
	}

	unit->m_previn = ZIN0(0);
	ZOUT0(0) = unit->mLevel = 0.f;
}

void Phasor_next_kk(Phasor *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	
	float in        = ZIN0(0);
	float rate      = ZIN0(1);
	float start     = ZIN0(2);
	float end       = ZIN0(3);
	float resetPos  = ZIN0(4);
	
	float previn = unit->m_previn;
	float level  = unit->mLevel;
	
	if (previn <= 0.f && in > 0.f) {
		level = resetPos;
	}
	LOOP(inNumSamples,
		level = sc_wrap(level, start, end);
		
		ZXP(out) = level;
		level += rate;
	);
	
	unit->m_previn = in;
	unit->mLevel = level;
}

void Phasor_next_ak(Phasor *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	
	float *in       = ZIN(0);
	float rate      = ZIN0(1);
	float start     = ZIN0(2);
	float end       = ZIN0(3);
	float resetPos  = ZIN0(4);
	
	float previn = unit->m_previn;
	float level  = unit->mLevel;
	
	LOOP(inNumSamples,
		float curin = ZXP(in);
		if (previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = resetPos + frac * rate;
		} else {
			level += rate;
		}
		level = sc_wrap(level, start, end);
		
		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->mLevel = level;
}

void Phasor_next_aa(Phasor *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in       = ZIN(0);
	float *rate     = ZIN(1);
	float start     = ZIN0(2);
	float end       = ZIN0(3);
	float resetPos  = ZIN0(4);
	
	float previn = unit->m_previn;
	float level = unit->mLevel;
	
	LOOP(inNumSamples,
		float curin = ZXP(in);
		float zrate = *rate++;
		if (previn <= 0.f && curin > 0.f) {
			float frac = -previn/(curin-previn);
			level = resetPos + frac * zrate;
		} else {
			level += zrate;
		}
		level = sc_wrap(level, start, end);

		ZXP(out) = level;
		previn = curin;
	);
	
	unit->m_previn = previn;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void Peak_Ctor(Peak *unit)
{
	if (INRATE(1) == calc_FullRate) {
		SETCALC(Peak_next_aa);
	} else if (INRATE(1) == calc_ScalarRate) {
		SETCALC(Peak_next_ai);
	} else {
		SETCALC(Peak_next_ak);
	}
	unit->m_prevtrig = 0.f;
	ZOUT0(0) = unit->mLevel = ZIN0(0);
}

void Peak_next_ak(Peak *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float curtrig = ZIN0(1);
	float level = unit->mLevel;
	float inlevel;
	LOOP(inNumSamples,
		inlevel = ZXP(in);
		if (inlevel > level) level = inlevel;
		ZXP(out) = level;
	);
	if (unit->m_prevtrig <= 0.f && curtrig > 0.f) level = inlevel;
	unit->m_prevtrig = curtrig;
	unit->mLevel = level;
}

void Peak_next_ai(Peak *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float level = unit->mLevel;
	float inlevel;
	LOOP(inNumSamples,
		inlevel = ZXP(in);
		if (inlevel > level) level = inlevel;
		ZXP(out) = level;
	);
	unit->mLevel = level;
}

void Peak_next_aa(Peak *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float *trig = ZIN(1);
	float prevtrig = unit->m_prevtrig;
	float level = unit->mLevel;

	LOOP(inNumSamples,
		float curtrig = ZXP(trig);
		float inlevel = ZXP(in);
		if (inlevel > level) level = inlevel;
		ZXP(out) = level;
		if (prevtrig <= 0.f && curtrig > 0.f) level = inlevel;
		prevtrig = curtrig;
	);
	unit->m_prevtrig = prevtrig;
	unit->mLevel = level;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void MostChange_Ctor(MostChange *unit)
{
	if (INRATE(0) == calc_FullRate) {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(MostChange_next_aa);
		} else {
			SETCALC(MostChange_next_ak);
		}
	} else {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(MostChange_next_ka);
		} else {
			SETCALC(MostChange_next_aa);
		}
	}
	unit->mPrevA = 0.f;
	unit->mPrevB = 0.f;
	unit->mRecent = 1;
	MostChange_next_aa(unit, 1);
}

void MostChange_next_ak(MostChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *a = ZIN(0);
	float xb = ZIN0(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xa = ZXP(a);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff > 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff < 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}

void MostChange_next_aa(MostChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *a = ZIN(0);
	float *b = ZIN(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xa = ZXP(a);
		float xb = ZXP(b);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff > 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff < 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}

void MostChange_next_ka(MostChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float xa = ZIN0(0);
	float *b = ZIN(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xb = ZXP(b);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff > 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff < 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}


////////////////////////////////////////////////////////////////////////////////////////////////////////

void LeastChange_Ctor(LeastChange *unit)
{
	if (INRATE(0) == calc_FullRate) {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(LeastChange_next_aa);
		} else {
			SETCALC(LeastChange_next_ak);
		}
	} else {
		if (INRATE(1) == calc_FullRate) {
			SETCALC(LeastChange_next_ka);
		} else {
			SETCALC(LeastChange_next_aa);
		}
	}
	unit->mPrevA = 0.f;
	unit->mPrevB = 0.f;
	unit->mRecent = 0;
	LeastChange_next_aa(unit, 1);
}

void LeastChange_next_ak(LeastChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *a = ZIN(0);
	float xb = ZIN0(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xa = ZXP(a);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff < 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff > 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}

void LeastChange_next_aa(LeastChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *a = ZIN(0);
	float *b = ZIN(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xa = ZXP(a);
		float xb = ZXP(b);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff < 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff > 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}

void LeastChange_next_ka(LeastChange *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float xa = ZIN0(0);
	float *b = ZIN(1);
	float prevA = unit->mPrevA;
	float prevB = unit->mPrevB;
	int recent = unit->mRecent;
	LOOP(inNumSamples,
		float xb = ZXP(b);
		float diff = fabs(xa - prevA) - fabs(xb - prevB);
		if (diff < 0.f) {
			recent = 0;
			ZXP(out) = xa;
		} else if (diff > 0.f) {
			recent = 1;
			ZXP(out) = xb;
		} else {
			ZXP(out) = recent ? xb : xa;
		}
		prevA = xa;
		prevB = xb;
	);
	unit->mPrevA = prevA;
	unit->mPrevB = prevB;
	unit->mRecent = recent;
}


////////////////////

void LastValue_Ctor(LastValue *unit)
{
	if (INRATE(0) == calc_FullRate) {
			SETCALC(LastValue_next_ak);
		} else {
			SETCALC(LastValue_next_kk);
		}
	
	unit->mPrev = ZIN0(0);
	unit->mCurr = ZIN0(0);
	LastValue_next_kk(unit, 1);
}

void LastValue_next_kk(LastValue *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float inval = ZIN0(0);
	float delta = ZIN0(1);
	float diff = fabs(inval - unit->mCurr);
	if(diff >= delta) { 
		unit->mPrev = unit->mCurr; 
		unit->mCurr = inval;
	}
	float level = unit->mPrev;
	LOOP(inNumSamples, ZXP(out) = level; );
	
}


void LastValue_next_ak(LastValue *unit, int inNumSamples)
{
	float *out = ZOUT(0);
	float *in = ZIN(0);
	float delta = ZIN0(1);
	float prev = unit->mPrev;
	float curr = unit->mCurr;
	
	LOOP(inNumSamples,
		float inval = ZXP(in);
		float diff = fabs(inval - curr);
		if(diff >= delta) { 
				prev = curr; 
				curr = inval;
		}
		ZXP(out) = prev
	);
	unit->mPrev = prev;
	unit->mCurr = curr;

}



//////////////////////////////////////////////////////////////////////////////////////////

void Done_Ctor(Done *unit)
{
	SETCALC(Done_next);

	unit->m_src = unit->mInput[0]->mFromUnit;
	
	Done_next(unit, 1);
}



void Done_next(Done *unit, int inNumSamples)
{
	float *out = OUT(0);
	Unit *src = unit->m_src;
	if (src) *out = src->mDone ? 1.f : 0.f;
	else *out = 0.f;
}

//////////////////////////////////////////////////////////////////////////////////////////

void FreeSelf_Ctor(FreeSelf *unit)
{
	SETCALC(FreeSelf_next);
		
	FreeSelf_next(unit, 1);
}


void FreeSelf_next(FreeSelf *unit, int inNumSamples)
{
	float in = ZIN0(0);
	if (in > 0.f) {
		NodeEnd(&unit->mParent->mNode);
		SETCALC(ClearUnitOutputs);
	}
	ZOUT0(0) = in;
}

//////////////////////////////////////////////////////////////////////////////////////////

void PauseSelf_Ctor(PauseSelf *unit)
{
	SETCALC(PauseSelf_next);		
	unit->m_prevtrig = 0.f;
	PauseSelf_next(unit, 1);
}


void PauseSelf_next(PauseSelf *unit, int inNumSamples)
{
	float in = ZIN0(0);
	if (in > 0.f && unit->m_prevtrig <= 0.f) {
		NodeRun(&unit->mParent->mNode, 0);
	}
	unit->m_prevtrig = in;
}

//////////////////////////////////////////////////////////////////////////////////////////

void Pause_Ctor(Pause *unit)
{
	SETCALC(Pause_next);

	unit->m_state = -99;
	
	ZOUT0(0) = ZIN0(0);
}


void Pause_next(Pause *unit, int inNumSamples)
{
	float in = ZIN0(0);
	int state = in != 0.f;
	if (state != unit->m_state) {
		unit->m_state = state;
		int id = (int)ZIN0(1);
		Node *node = SC_GetNode(unit->mWorld, id);
		if (node) {
			NodeRun(node, state);
		}
		SETCALC(ClearUnitOutputs);
	}
	ZOUT0(0) = in;
}

//////////////////////////////////////////////////////////////////////////////////////////

void Free_Ctor(Free *unit)
{
	SETCALC(Free_next);

	unit->m_state = -99;
	
	ZOUT0(0) = ZIN0(0);
}


void Free_next(Free *unit, int inNumSamples)
{
	float in = ZIN0(0);
	int state = in != 0.f;
	if (state != unit->m_state) {
		unit->m_state = state;
		int id = (int)ZIN0(1);
		Node *node = SC_GetNode(unit->mWorld, id);
		if (node) {
			NodeEnd(node);
		}
		SETCALC(ClearUnitOutputs);
	}
	ZOUT0(0) = in;
}

//////////////////////////////////////////////////////////////////////////////////////////

void FreeSelfWhenDone_Ctor(FreeSelfWhenDone *unit)
{
	
	unit->m_src = unit->mInput[0]->mFromUnit;
	
	if (unit->m_src) {
		SETCALC(FreeSelfWhenDone_next);
		FreeSelfWhenDone_next(unit, 1);
	} else {
		SETCALC(ClearUnitOutputs);
		ClearUnitOutputs(unit, 1);
	}
}


void FreeSelfWhenDone_next(FreeSelfWhenDone *unit, int inNumSamples)
{
	float *out = OUT(0);
	float *in = IN(0);
	Unit *src = unit->m_src;
	if (src->mDone) {
		NodeEnd(&unit->mParent->mNode);
		SETCALC(ClearUnitOutputs);
	}
	*out = *in;
}

//////////////////////////////////////////////////////////////////////////////////////////

void PauseSelfWhenDone_Ctor(PauseSelfWhenDone *unit)
{
	
	unit->m_src = unit->mInput[0]->mFromUnit;
	
	if (unit->m_src) {
		SETCALC(PauseSelfWhenDone_next);
		PauseSelfWhenDone_next(unit, 1);
	} else {
		SETCALC(ClearUnitOutputs);
		ClearUnitOutputs(unit, 1);
	}
}


void PauseSelfWhenDone_next(PauseSelfWhenDone *unit, int inNumSamples)
{
	float *out = OUT(0);
	float *in = IN(0);
	Unit *src = unit->m_src;
	if (src->mDone) {
		NodeRun(&unit->mParent->mNode, 0);
		SETCALC(ClearUnitOutputs);
	}
	*out = *in;
}

////////////////////////////////////////////////////////////////////////////////////////////////////////

void load(InterfaceTable *inTable)
{
	ft = inTable;

	DefineSimpleUnit(Trig1);
	DefineSimpleUnit(Trig);
	DefineSimpleUnit(SendTrig);
	DefineSimpleUnit(ToggleFF);
	DefineSimpleUnit(SetResetFF);
	DefineSimpleUnit(Latch);
	DefineSimpleUnit(Gate);
	DefineSimpleUnit(Schmidt);
	DefineSimpleUnit(PulseDivider);
	DefineSimpleUnit(PulseCount);
	DefineSimpleUnit(TDelay);
	DefineSimpleUnit(ZeroCrossing);
	DefineSimpleUnit(Timer);
	DefineSimpleUnit(Sweep);
	DefineSimpleUnit(Phasor);
	DefineSimpleUnit(Peak);
	DefineSimpleUnit(MostChange);
	DefineSimpleUnit(LeastChange);
	DefineSimpleUnit(LastValue);
	DefineSimpleUnit(Done);
	DefineSimpleUnit(Pause);
	DefineSimpleUnit(FreeSelf);
	DefineSimpleUnit(PauseSelf);
	DefineSimpleUnit(FreeSelfWhenDone);
	DefineSimpleUnit(PauseSelfWhenDone);
}
