package stachelsau.snare.proc;

import stachelsau.snare.proc.ProcessManager.BaseProcess;

public class DelayProc extends BaseProcess {

    private float mDelay;
    private float mWaited;
    
    /**
     * Constructor.
     * @param delay Delay in ms.
     */
    public DelayProc(float delay) {
        mDelay = delay;
    }
    
    @Override
    public void onInit() {
        mWaited = 0;
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mWaited += virtualDelta;
        if (mWaited >= mDelay) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onKill() {
        
    }

}
