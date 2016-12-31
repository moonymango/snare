package com.moonymango.snare.util;

import com.moonymango.snare.game.Game;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Some interpolation profiles. This maps an input value in range [0..1] 
 * to an output value also in range [0..1]. 
 */
public enum EasingProfile {
    /** Linear: y=x, not really a profile. */
    LINEAR,
    /** Random mapping */
    RANDOM,
    /** y = sin(x*pi/2) */
    SIN_IN,
    /** y = 1-cos(x*pi/2) */
    SIN_OUT,
    /** y = sin²(x*pi/2) */
    SIN_2,
    /** y = x² */
    SQUARE_OUT,
    /** y = -x²+2x */
    SQUARE_IN,
    QUART_OUT,
    QUART_IN,
    BOUNCE,
    ELASTIC;
    
    private static final BounceInterpolator sB = new BounceInterpolator();
    private static final OvershootInterpolator mO = new OvershootInterpolator(1.5f);
    
    /**
     * Interpolate by profile.
     * @param x Value in [0..1]
     * @return Value in [0..1]
     */
    public float value(float x) {
        switch(this) {
        case LINEAR: return Math.max(0, Math.min(x, 1));
        case RANDOM: return Game.get().getRandomFloat(0, 1);
        case SIN_IN: return (float) Math.sin(x*Geometry.RAD90);
        case SIN_OUT: return (float) (1-Math.cos(x*Geometry.RAD90));
        case SIN_2:
            final float sin = (float) Math.sin(x*Geometry.RAD90);
            return sin*sin;
        case SQUARE_OUT: return x*x;
        case SQUARE_IN: return -(x*x)+2*x;
        case QUART_OUT:
            float f = x*x;
            return f*f;
        case QUART_IN: 
            f = (x-1)*(x-1);
            return -f*f+1;
        case BOUNCE: return sB.getInterpolation(x);
        case ELASTIC: 
            //return (float) (1 + (float)Math.pow(2, -5*x) * Math.sin((x-0.1f) * Geometry.RAD360));
            return mO.getInterpolation(x);
        default:
        }
        return 0;
    }

}
