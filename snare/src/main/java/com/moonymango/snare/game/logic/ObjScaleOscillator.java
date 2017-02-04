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
    private float mLocalTime;

    // oscillation parameters for each dimensions
    private float[] mAmpl = new float[3];
    private float[] mFreq = new float[3];
    private float[] mPhase = new float[3];


    /**
     * Configures the oscillation.
     * @param obj       Object to scale.
     * @param clock     Time base to use.
     * @param amplitudeX Amplitude in x dimension [0..1].
     * @param amplitudeY Amplitude in y dimension [0..1].
     * @param amplitudeZ Amplitude in z dimension [0..1].
     * @param periodX    Period in ms for x dimension.
     * @param periodY    Period in ms for y dimension.
     * @param periodZ    Period in ms for z dimension.
     * @param phaseX     Phase in degrees for x dimension.
     * @param phaseY     Phase in degrees for y dimension.
     * @param phaseZ     Phase in degrees for z dimension.*
     * @return this
     */
    public ObjScaleOscillator configure(GameObj obj, Game.ClockType clock,
                                        float amplitudeX, float amplitudeY, float amplitudeZ,
                                        float periodX, float periodY, float periodZ,
                                        float phaseX, float phaseY, float phaseZ)
    {
        mObj = obj;
        mClock = clock;

        mAmpl[0] = amplitudeX;
        mAmpl[1] = amplitudeY;
        mAmpl[2] = amplitudeZ;

        mFreq[0] = Geometry.RAD360 / periodX;
        mFreq[1] = Geometry.RAD360 / periodY;
        mFreq[2] = Geometry.RAD360 / periodZ;

        mPhase[0] = Geometry.toRadian(phaseX);
        mPhase[1] = Geometry.toRadian(phaseY);
        mPhase[2] = Geometry.toRadian(phaseZ);

        // get actual scale
        final float[] tmp = mObj.getScale();
        System.arraycopy(tmp, 0, mOrigScale, 0, 4);
        return this;
    }


    /**
     * Configures the oscillation with same parameters for all dimensions.
     * @param obj           Object to scale.
     * @param clock         Time base to use.
     * @param amplitude     Amplitude of oscillation [0..1]
     * @param period        Oscillation period in milliseconds.
     * @return this
     */
    public ObjScaleOscillator configure(GameObj obj, Game.ClockType clock, float amplitude,
                          float period)
    {
        mObj = obj;
        mClock = clock;

        // calculate angular frequency
        final float f = Geometry.RAD360 / period;

        for (int i = 0; i < 3; i++)
        {
            mAmpl[i] = amplitude;
            mFreq[i] = f;
            mPhase[i] = 0;
        }

        // get actual scale
        final float[] tmp = mObj.getScale();
        System.arraycopy(tmp, 0, mOrigScale, 0, 4);

        return this;
    }


    @Override
    protected void onInit()
    {
        mLocalTime = 0;
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta, float virtualDelta)
    {
        mLocalTime += (mClock == Game.ClockType.REALTIME) ? realDelta : virtualDelta;

        final float sx = 1f + (float) (mAmpl[0] * Math.sin(mFreq[0]*mLocalTime + mPhase[0]));
        final float sy = 1f + (float) (mAmpl[1] * Math.sin(mFreq[1]*mLocalTime + mPhase[1]));
        final float sz = 1f + (float) (mAmpl[2] * Math.sin(mFreq[2]*mLocalTime + mPhase[2]));
        mObj.setScale(mOrigScale[0]*sx, mOrigScale[1]*sy, mOrigScale[2]*sz);

        return true;
    }

    @Override
    protected void onKill()
    {
    }
}
