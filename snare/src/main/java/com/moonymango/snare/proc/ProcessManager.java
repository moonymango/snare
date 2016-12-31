package com.moonymango.snare.proc;

import java.util.ArrayList;
import java.util.List;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.util.PoolItem;

/**
 * Manager for all derivates of {@link BaseProcess}. A process is 
 * intended for all computations/actions which span over multiple
 * frames. When a process runs, it's update function gets called 
 * every frame. 
 * There are two ways to stop a process: 
 * 1) externally by calling it's kill() method. 
 * 2) the process can stop itself by returning false in onUpdate().
 * 
 * {@link BaseProcess} is also a {@link PoolItem}, recycle() is
 * called when the process stops. So it is possible to have
 * "fire-and-forget" processes which recycle themselves.
 */
public class ProcessManager {
    
    private final List<BaseProcess> mProcs = new ArrayList<BaseProcess>();
   
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {
        for (int i = mProcs.size() - 1; i >= 0; i--) {
            BaseProcess proc = mProcs.get(i);
            switch (proc.mState) {
            
            case INIT:
                proc.onInit();
                proc.mState = ProcState.WAIT;
                break;
                
            case WAIT:
                final float d = proc.mDelayClock == ClockType.REALTIME ?
                        realDelta : virtualDelta;
                proc.mDelayTimer -= d;
                if (proc.mDelayTimer > 0) {
                    break;
                }
                proc.mState = ProcState.RUNNING;
                
            case RUNNING:
                if (proc.mLastUpdate != realTime) {
                    if (!proc.onUpdate(realTime, realDelta, virtualDelta)) {
                        proc.mState = ProcState.DEAD;
                    }
                    proc.mLastUpdate = realTime;
                }
                break;
                
            case PAUSED:
                break; // do nothing
                
            case DEAD:
            case DEAD_CHAIN:
                proc.onKill();
                if (proc.mListener != null) {
                    proc.mListener.onProcessKilled(proc);
                }
                
                BaseProcess next = proc.mNext;
                if (next != null && proc.mState != ProcState.DEAD_CHAIN) {
                    attach(next);
                }                    
                
                mProcs.remove(proc);
                proc.recycle();
            }
        }
    }
    
    public int getProcessCount() {
        return mProcs.size();
    }
    
    protected void attach(BaseProcess proc) {
        mProcs.add(proc);
        //proc.onInit();
        proc.mState = ProcState.INIT;
        proc.mDelayTimer = proc.mDelay;
    }
    
    public enum ProcState {
        INIT,
        WAIT,
        RUNNING,
        PAUSED,
        DEAD,       // process is dead, successors will be run
        DEAD_CHAIN  // process is dead, successors won't be run
    }
    
    public interface IOnProcessKilledListener {
        void onProcessKilled(BaseProcess proc); 
    }
    
    /**
     * Process base class. Processes can be chained, i.e. a
     * process automatically starts when its predecessor in the
     * chain gets killed. Also circular chains are allowed. 
     */
    public static abstract class BaseProcess extends PoolItem {

        private ClockType mDelayClock;
        private float mDelay;
        private float mDelayTimer;
        private BaseProcess mNext;
        private ProcState mState = ProcState.DEAD;
        private long mLastUpdate;
        private IOnProcessKilledListener mListener;
        
        protected BaseProcess() {
            this(ClockType.REALTIME, 0, null);
        }
        
        /**
         * @param listener Listener which gets notified when process was killed.
         */
        protected BaseProcess(IOnProcessKilledListener listener) {
            this(ClockType.REALTIME, 0, listener);
        }
        
        /**
         * Process with delay. After attaching the process to 
         * {@link ProcessManager} callbacks to onUpdate() start not until
         * the delay has passed. onInit() is called immediately when
         * proc is attached.
         * 
         * @param delayClock Clock to be used for delay.
         * @param delay Delay.
         * @param listener Listener which gets notified when process was killed.
         */
        protected BaseProcess(ClockType delayClock, float delay, 
                IOnProcessKilledListener listener) {
            mListener = listener;
            mDelayClock = delayClock;
            mDelay = delay;
        }
        
        /**
         * @param listener Listener which gets notified when process was killed.
         */
        public void setListener(IOnProcessKilledListener listener) {
            mListener = listener;
        }
        
        public void setDelay(ClockType clock, float delay) {
            if (mState == ProcState.DEAD) {
                mDelayClock = clock;
                mDelay = delay;
            }
        }
        
        /** Called after process has been attached to ProcessManager */
        protected abstract void onInit();
        /**
         * Gets called every frame. Calls will start after delay has passed.
         * @param realDelta
         * @param virtualDelta
         * @return True, when process should be called again next frame, false otherwise.
         */
        protected abstract boolean onUpdate(long realTime, float realDelta, float virtualDelta);
        
        /** 
         * Callback. Gets called when process is detached from process manager.
         */
        protected abstract void onKill();
         
        /**
         * Sets the predecessor in process chain.
         * @param next
         * @return
         */
        public BaseProcess setNext(BaseProcess next) {
            mNext = next;
            return next;
        }
        
        /**
         * Returns predecessor in process chain.
         * @return
         */
        public BaseProcess getNext() {
            return mNext;
        }
        
        /**
         * Finds the last process in the next chain (see setNext()).
         * In case of a circular chain, a reference to this very
         * process is returned. 
         * @return Last process of chain.  
         */
        public BaseProcess findLast() {
            BaseProcess result = this;
            while (result.mNext != null) {
                result = result.mNext;
                if (result == this) {
                    // prevent endless loop for circular chain
                    return this;
                }
            }
            return result;
        }
        
        /**
         * Starts the process. Will only have an effect, if
         * process is currently not attached to the {@link ProcessManager}
         */
        public void run() {
            if (isDead()) {
                Game.get().getProcManager().attach(this);
            }
        }
        
        /**
         * Kills the process immediately. The next process in the
         * chain will be run. 
         * Note: Another way to kill the process is 
         * to return false in onUpdate()
         */
        public void kill() {
            mState = ProcState.DEAD;
        }
        
        /**
         * Kills this process and all following processes 
         * in the chain immediately.
         */
        public void killChain() { 
            if (mState != ProcState.DEAD_CHAIN) {
                mState = ProcState.DEAD_CHAIN;
                if (mNext != null) {
                    mNext.killChain();
                }
            }
        }
        
        
        public boolean isRunning() {
            return mState == ProcState.RUNNING;
        }
        
        public boolean isDead() {
            return mState == ProcState.DEAD || mState == ProcState.DEAD_CHAIN;
        }
        
        public ProcState getState() {
            return mState;
        }

        /** 
         * Pauses the process. The process stays attached to
         * the {@link ProcessManager} but onUpdate() will not 
         * be called until the process is resumed. 
         **/
        public void pause() {
            if (mState == ProcState.RUNNING) {
                mState = ProcState.PAUSED;
            }
        }
        
        /**
         * Resumes the process.
         */
        public void resume() {
            if (mState == ProcState.PAUSED) {
                mState = ProcState.RUNNING;
            }
        }
    }
}
