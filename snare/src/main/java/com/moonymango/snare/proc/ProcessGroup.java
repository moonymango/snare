package com.moonymango.snare.proc;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.ProcState;

import java.util.ArrayList;

/**
 * Provides handling of a group of processes as if it was a single process.
 * Note: No support for process chains!
 */
public class ProcessGroup extends BaseProcess {

    private ArrayList<BaseProcess> mProcs = new ArrayList<>();

    public ProcessGroup(IGame game)
    {
        super(game);
    }

    @Override
    protected void onInit() {
        // start all processes
        for (int i = 0; i < mProcs.size(); i++) {
            mProcs.get(i).run();
        }
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        // check if at least one process is still running
        boolean run = false;
        for (int i = 0; i < mProcs.size(); i++) {
            run |= mProcs.get(i).getState() != ProcState.DEAD;
        }
        return run;
    }

    @Override
    protected void onKill() {
        // stop all processes
        for (int i = 0; i < mProcs.size(); i++) {
            mProcs.get(i).kill();
        }
    }
    
    public void addProc(BaseProcess proc) {
        if (getState() != ProcState.DEAD) {
            throw new IllegalStateException("Cannot add process to running " +
            		"process group.");
        }
        if (proc != null) {
            mProcs.add(proc);
        }
    }

}
