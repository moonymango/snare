package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.util.IEasingProfile;
import com.moonymango.snare.util.QuaternionAF;

/**
 * Changes object state (position, rotation, scale) over time.
 */
public class ObjRotationTransition extends BaseProcess {

    private final GameObj mObj;
    private final GameObj mStart;
    private final GameObj mEnd;
    
    private final float[] mStartRotQ = new float[4];
    private final float[] mEndRotQ = new float[4];
    private final float mDuration;
    private final IEasingProfile mProfile;
    private ClockType mClock = ClockType.VIRTUAL;
    
    private float mTime;
    
    /**
     * Constructor using dedicated object to define start state.
     * @param obj Object to change.
     * @param start Object describing start state.
     * @param end Object describing end state.
     * @param time time in milliseconds
     */
    public ObjRotationTransition(GameObj obj, GameObj start, GameObj end, 
            float time, IEasingProfile profile) {
        mObj = obj;
        mStart = start;
        mEnd = end;
        mDuration = time;
        mProfile = profile;
    }
    
    public ObjRotationTransition(GameObj obj, GameObj end, float time, 
            IEasingProfile profile) {
        this(obj, obj, end, time, profile);
    }
    
    public void setClockType(ClockType clock) {
        mClock = clock;
    }
    
    @Override
    protected void onInit() {
        // store start and end rotation
        float[] tmp = mStart.getRotationQ();
        for (int i = 0; i < 4; i++) {
            mStartRotQ[i] = tmp[i];
        }
        tmp = mEnd.getRotationQ();
        for (int i = 0; i < 4; i++) {
            mEndRotQ[i] = tmp[i];
        }
        
        mTime = 0;
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        mTime += mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        if (mTime >= mDuration) {
            // set to exact end state
            mObj.setRotationQ(mEndRotQ);
            return false;
        }
        
        // interpolate
        final float t = mProfile.value(mTime/mDuration);
        final float[] rot = QuaternionAF.slerp(mStartRotQ, mEndRotQ, t, true);
        mObj.setRotationQ(rot);
        return true;
    }

    @Override
    protected void onKill() {}

}
