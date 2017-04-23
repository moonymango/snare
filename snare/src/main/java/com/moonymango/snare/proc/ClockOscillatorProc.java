package com.moonymango.snare.proc;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.util.Geometry;

/**
 * Varies the game's virtual time sinusoidally.
 */
public class ClockOscillatorProc extends BaseProcess
{
    private final long mPeriod;
    private final float mAmplitude;
    private float mOriginalTimeFactor;
    private float mProgress;
    
    /**
     * @param period Period based on realtime.
     * @param amplitude
     */
    public ClockOscillatorProc(IGame game, long period, float amplitude)
    {
        super(game);
        mPeriod = period;
        mAmplitude = amplitude;
    }
    
    @Override
    protected void onInit() {
        mOriginalTimeFactor = mGame.getVirtualTimeFactor();
        if (mOriginalTimeFactor - mAmplitude < 0) {
            throw new IllegalStateException("Oscillation will result in negative virtual time.");
        }
        mProgress = 0;
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        mProgress += realDelta;
        final float t = (float) (mAmplitude * Math.sin(Geometry.RAD360 * mProgress / mPeriod)
                + mOriginalTimeFactor);
        mGame.setVirtualTimeFactor(t);
        return true;
    }

    @Override
    protected void onKill() {
        
    }

}
