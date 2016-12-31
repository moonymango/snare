package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.util.EasingProfile;

public class ObjPosTransition extends BaseProcess {

    private final GameObj mObj;
    private final GameObj mStart;
    private final GameObj mEnd;
    
    private final float[] mStartPos = new float[4];
    private final float[] mDelta = new float[4];
    private final float[] mEndPos = new float[4];
    private final float mDuration;
    private final EasingProfile mProfile;
    private ClockType mClock = ClockType.VIRTUAL;
    
    private float mTime;
    
    /**
     * Constructor using dedicated object to define start state.
     * @param obj Object to change.
     * @param start Object describing start state.
     * @param end Object describing end state.
     * @param time time in milliseconds
     */
    public ObjPosTransition(GameObj obj, GameObj start, GameObj end, 
            float time, EasingProfile profile) {
        mObj = obj;
        mStart = start;
        mEnd = end;
        mDuration = time;
        mProfile = profile;
    }
    
    public ObjPosTransition(GameObj obj, GameObj end, float time, 
            EasingProfile profile) {
        this(obj, obj, end, time, profile);
    }
    
    public void setClockType(ClockType clock) {
        mClock = clock;
    }
    
    @Override
    protected void onInit() {
        // store start and end positions
        float[] tmp = mStart.getPosition();
        for (int i = 0; i < 4; i++) {
            mStartPos[i] = tmp[i];
        }
        tmp = mEnd.getPosition();
        for (int i = 0; i < 4; i++) {
            mEndPos[i] = tmp[i];
        }
        mDelta[0] = (tmp[0] - mStartPos[0]);
        mDelta[1] = (tmp[1] - mStartPos[1]);
        mDelta[2] = (tmp[2] - mStartPos[2]);
        
        mTime = 0;

    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        mTime += mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        if (mTime >= mDuration) {
            // set to exact end state
            mObj.setPosition(mEndPos[0], mEndPos[1], mEndPos[2]);
            return false;
        }
        
        // interpolate
        final float t = mProfile.value(mTime/mDuration);
        mObj.setPosition(mStartPos[0] + mDelta[0]*t, 
                mStartPos[1] + mDelta[1]*t, 
                mStartPos[2] + mDelta[2]*t);
        return true;
    }

    @Override
    protected void onKill() {}

}
