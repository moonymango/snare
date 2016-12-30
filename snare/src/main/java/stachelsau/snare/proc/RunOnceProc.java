package stachelsau.snare.proc;

import stachelsau.snare.proc.ProcessManager.BaseProcess;

public class RunOnceProc extends BaseProcess {

    private final Runnable mTarget;
        
    public RunOnceProc(Runnable target) {
        if (target == null) {
            throw new IllegalArgumentException("Missing runnable.");
        }
        mTarget = target;
    }
    
        
    @Override
    public void onInit() {}

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mTarget.run();
        return false;
    }


    @Override
    protected void onKill() {
        
    }

}
