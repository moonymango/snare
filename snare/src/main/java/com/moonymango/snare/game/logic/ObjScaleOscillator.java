package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.proc.ProcessManager;
import com.moonymango.snare.util.Geometry;

/**
 * Sine oscillation of game object scale.
 */

public class ObjScaleOscillator extends ProcessManager.BaseProcess
{
    private GameObj mObj;
    private float[] mOrigScale = new float[4];
    private Game.ClockType mClock;
    private float mAmpl, mFreq;
    private float mLocalTime;

    /**
     * Configures the oscillation.
     * @param obj           Object to scale.
     * @param clock         Time base to use.
     * @param amplitude     Amplitude of oscillation [0..1]
     * @param period        Oscillation period in milliseconds.
     * @param phase         Phase in degrees.
     */
    public void configure(GameObj obj, Game.ClockType clock, float amplitude,
                          float period, float phase)
    {
        mObj = obj;
        mClock = clock;
        mAmpl = amplitude;

        // calculate angular frequency
        mFreq = Geometry.RAD360 / period;

        // offset local time for phase
        mLocalTime = period / Geometry.RAD360 * phase;

        // get actual scale
        final float[] tmp = mObj.getScale();
        System.arraycopy(tmp, 0, mOrigScale, 0, 4);
    }


    @Override
    protected void onInit()
    {
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta, float virtualDelta)
    {
        mLocalTime += (mClock == Game.ClockType.REALTIME) ? realDelta : virtualDelta;
        final float s = 1f + (float) (mAmpl * Math.sin(mFreq*mLocalTime));
        mObj.setScale(mOrigScale[0]*s, mOrigScale[1]*s, mOrigScale[2]*s);

        return true;
    }

    @Override
    protected void onKill()
    {
    }
}
