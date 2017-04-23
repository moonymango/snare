package com.moonymango.snare.proc;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

public class DelayProc extends BaseProcess {

    private float mDelay;
    private float mWaited;
    
    /**
     * Constructor.
     * @param delay Delay in ms.
     */
    public DelayProc(IGame game, float delay)
    {
        super(game);
        mDelay = delay;
    }
    
    @Override
    public void onInit() {
        mWaited = 0;
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mWaited += virtualDelta;
        return mWaited < mDelay;
    }

    @Override
    protected void onKill() {
        
    }

}
