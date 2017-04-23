package com.moonymango.snare.proc;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;


public class UpdateProc extends BaseProcess {

    private final IUpdateable mTarget;
    
    public UpdateProc(IGame game, IUpdateable target)
    {
        super(game);
        mTarget = target;
    }
    
    @Override
    public void onInit() {}

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mTarget.onUpdate(realTime, realDelta, virtualDelta);
        return true;
    }

    public interface IUpdateable {
        void onUpdate(long realTime, float realDelta, float virtualDelta);
    }

    @Override
    protected void onKill() {
        
    }
    
}
