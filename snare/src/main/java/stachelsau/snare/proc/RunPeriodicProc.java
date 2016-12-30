package stachelsau.snare.proc;

import stachelsau.snare.proc.ProcessManager.BaseProcess;

public class RunPeriodicProc extends BaseProcess {

    private final Runnable mTarget;
    private final float mPeriod;
    private float mWaited;
    
    public RunPeriodicProc(Runnable target, float period) {
        if (target == null) {
            throw new IllegalArgumentException("Missing runnable.");
        }
        mTarget = target;
        mPeriod = period;
    }

    @Override
    public void onInit() {
        mWaited = 0;
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mWaited += virtualDelta;
        if (mWaited >= mPeriod) {
            mTarget.run();
            mWaited = 0;
        }
        return true;
    }

    @Override
    protected void onKill() {
        
    }

}
