package stachelsau.snare.game.logic;

import stachelsau.snare.game.Game.ClockType;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.proc.ProcessManager.BaseProcess;
import stachelsau.snare.proc.ProcessManager.ProcState;
import stachelsau.snare.util.EasingProfile;

public class ObjScaleTransition extends BaseProcess {

    private GameObj mObj;
    private final GameObj mStart;
    private final GameObj mEnd;
   
    private final float[] mStartScale = {1, 1, 1, 1};
    private final float[] mDelta = {0, 0, 0, 0};
    private final float[] mEndScale = {1, 1, 1, 1};
    private float mDuration;
    private EasingProfile mProfile;
    private ClockType mClock = ClockType.VIRTUAL;
    
    private float mTime;
    
    /** 
     * Constructs unspecified transition. Has to be configured (see configure())
     * before starting the process.
     * @param obj
     * @param time
     * @param profile
     */
    public ObjScaleTransition() {
        mStart = null;
        mEnd = null;
    }
    
    /**
     * Constructs transition using the 
     * @param obj
     * @param start
     * @param end
     * @param time
     * @param profile
     */
    public ObjScaleTransition(GameObj obj, float[] start, float[] end,
            float time, EasingProfile profile) {
        
        mStart = null;
        mEnd = null;
        configure(obj, start, end, time, profile);
    }
    
    /**
     * Constructor using dedicated object to define start state and end state.
     * @param obj Object to change.
     * @param start Object describing start state.
     * @param end Object describing end state.
     * @param time time in milliseconds
     */
    public ObjScaleTransition(GameObj obj, GameObj start, GameObj end, 
            float time, EasingProfile profile) {
        mObj = obj;
        mStart = start;
        mEnd = end;
        mDuration = time;
        mProfile = profile;
    }
    
    /**
     * Constructor using dedicated object to define end state. Start state
     * is taken as is.
     * @param obj
     * @param end
     * @param time
     * @param profile
     */
    public ObjScaleTransition(GameObj obj, GameObj end, float time, 
            EasingProfile profile) {
        this(obj, obj, end, time, profile);
    }
    
    public ObjScaleTransition configure (GameObj obj, float[] start, float[] end, 
            float time, EasingProfile profile) 
    {
        if (getState() != ProcState.DEAD) {
            throw new IllegalArgumentException("Cannot change a running " +
                    "process.");
        }
        
        mObj = obj;
        mDuration = time;
        mProfile = profile;
        for (int i = 0; i < 4; i++) {
            mStartScale[i] = start[i];
            mEndScale[i] = end[i];
            mDelta[i] = end[i] - start[i];
        }
        return this;
    }
    
    public ObjScaleTransition setClockType(ClockType clock) {
        if (getState() != ProcState.DEAD) {
            throw new IllegalArgumentException("Cannot change a running " +
                    "process.");
        }
        mClock = clock;
        return this;
    }
    
    @Override
    protected void onInit() {
        
        mTime = 0;
        if (mStart == null || mEnd == null) {
            return;
        }
        
        // store start and end scale
        float[] tmp = mStart.getScale();
        for (int i = 0; i < 4; i++) {
            mStartScale[i] = tmp[i];
        }
        tmp = mEnd.getScale();
        for (int i = 0; i < 4; i++) {
            mEndScale[i] = tmp[i];
        }
        mDelta[0] = (tmp[0] - mStartScale[0]);
        mDelta[1] = (tmp[1] - mStartScale[1]);
        mDelta[2] = (tmp[2] - mStartScale[2]);        

    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        mTime += mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        if (mTime >= mDuration) {
            // set to exact end state
            mObj.setScale(mEndScale[0], mEndScale[1], mEndScale[2]);
            return false;
        }
        
        // interpolate
        final float t = mProfile.value(mTime/mDuration);
        mObj.setScale(mStartScale[0] + mDelta[0]*t, 
                mStartScale[1] + mDelta[1]*t, 
                mStartScale[2] + mDelta[2]*t);
        return true;
    }

    @Override
    protected void onKill() {}

}
