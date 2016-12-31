package com.moonymango.snare.proc;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

/**
 * Process which runs a {@link Runnable} in a separate thread.
 * Note: Do not access any active game engine objects from the 
 * runnable, because nothing there is thread safe! The single one 
 * exception from this is the {@link EventManager} event queue, so 
 * it is perfectly safe to queue events (not trigger!) from an 
 * {@link AsyncProc}.
 */
public class AsyncProc extends BaseProcess {

    private Thread mThread;
    private Runnable mRunnable;
    
    public AsyncProc(Runnable r) {
        mRunnable = r;
    }
    
    public void setRunnable(Runnable r) {
        mRunnable = r;
    }
    
    @Override
    protected void onInit() {
        mThread = new Thread(mRunnable);
        mThread.start();
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return mThread.isAlive();
    }
    
    @Override
    protected void onKill() {
        if (mThread.isAlive()) {
            mThread.interrupt();
        }
        while(mThread.isAlive()) {
            try {
                mThread.join();
            } catch (InterruptedException e) {}
        }
        mThread = null;
    }

}
