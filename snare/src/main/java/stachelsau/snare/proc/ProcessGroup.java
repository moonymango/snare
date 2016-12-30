package stachelsau.snare.proc;

import java.util.ArrayList;

import stachelsau.snare.proc.ProcessManager.BaseProcess;
import stachelsau.snare.proc.ProcessManager.ProcState;

/**
 * Provides handling of a group of processes as if it was a single process.
 * Note: No support for process chains!
 */
public class ProcessGroup extends BaseProcess {

    private ArrayList<BaseProcess> mProcs = 
            new ArrayList<ProcessManager.BaseProcess>();
    
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
