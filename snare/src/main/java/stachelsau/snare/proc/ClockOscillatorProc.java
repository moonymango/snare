package stachelsau.snare.proc;

import stachelsau.snare.game.Game;
import stachelsau.snare.proc.ProcessManager.BaseProcess;
import stachelsau.snare.util.Geometry;

/**
 * Varies the game's virtual time sinusoidally.
 */
public class ClockOscillatorProc extends BaseProcess{

    final long mPeriod;
    final float mAmplitude;
    float mOriginalTimeFactor;
    float mProgress;
    
    /**
     * @param period Period based on realtime.
     * @param amplitude
     */
    public ClockOscillatorProc(long period, float amplitude) {
        mPeriod = period;
        mAmplitude = amplitude;
    }
    
    @Override
    protected void onInit() {
        mOriginalTimeFactor = Game.get().getVirtualTimeFactor();
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
        Game.get().setVirtualTimeFactor(t);
        return true;
    }

    @Override
    protected void onKill() {
        
    }

}
