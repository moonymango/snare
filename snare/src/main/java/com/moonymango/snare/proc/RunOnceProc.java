package com.moonymango.snare.proc;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

public class RunOnceProc extends BaseProcess {

    private final Runnable mTarget;
        
    public RunOnceProc(IGame game, Runnable target)
    {
        super(game);
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
