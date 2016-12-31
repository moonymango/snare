package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.IOnProcessKilledListener;


/**
 * Generic state logic that can be configured with an
 * array of processes, which are started when the related 
 * game state gets activated. Next game state is triggered when
 * processes have finished execution.
 */
public class ProcessRunnerStateLogic implements IGameStateLogic {

    private IGameState mNext;
    private ProcChainWrapper[] mProcs;
    private boolean isActive;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        if (mProcs != null) {
            final int len = mProcs.length;
            for (int i = 0; i < len; i++) {
                if (mProcs[i].isRunning()) {
                    return null;
                }
            }
        }
        return mNext;
    }

    @Override
    public void onActivate(IGameState previous) {
        if (mProcs != null) {
            final int len = mProcs.length;
            for (int i = 0; i < len; i++) {
                mProcs[i].run();
            }
        }
        isActive = true;
    }

    @Override
    public void onDeactivate(IGameState next) {
        isActive = false;
    }

    @Override
    public void onInit() {}

    @Override
    public void onShutdown() {}

    @Override
    public void setNextState(IGameState next) {
        mNext = next;
    }

    /**
     * Set processes to run. Each process chain must be wrapped in 
     * {@link ProcChainWrapper} and placed into an array. 
     * Array must not contain null elements.
     * @param procs Array of process chains.
     */
    public void setProcs(ProcChainWrapper[] procs) {
        if (isActive) {
            throw new IllegalStateException("Cannot change processes while active.");
        }
        mProcs = procs;
    }
    
    public static class ProcChainWrapper implements IOnProcessKilledListener {

        private final BaseProcess mChain;
        private boolean mIsRunning;
        
        /**
         * @param chain First element of chain.
         */
        public ProcChainWrapper(BaseProcess chain) {
            mChain = chain;
            chain.findLast().setListener(this);
        }
        
        @Override
        public void onProcessKilled(BaseProcess proc) {
            mIsRunning = false;
        }
        
        /**
         * Start the chain.
         */
        public void run() {
            if (!mIsRunning) {
                mChain.run();
                mIsRunning = true;
            }
        }
        
        /**
         * @return true if any proc of chain is running, false otherwise.
         */
        public boolean isRunning() {
            return mIsRunning;
        }

    }

   
}
