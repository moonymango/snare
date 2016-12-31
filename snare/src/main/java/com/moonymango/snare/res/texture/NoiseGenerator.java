package com.moonymango.snare.res.texture;

import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.SimplexNoise;


/**
 * Generate variouse noise types. The getPixel() implementation delivers
 * always the same value, regardless of the requested channel. 
 */
public class NoiseGenerator implements ITextureChannelSource { 
    
    private float mStepX;
    private float mStepY;
    private final NoiseFunction mFunc;
    private final int mOctaves;
    private final float mX;
    private final float mY;
    private final float mW;
    private final float mH;
    
    /**
     * 
     * @param func Noise generator function.
     * @param octaves Number of octaves included into noise function.
     * @param x x offset of bitmap in 2D noise space.
     * @param y y offset of bitmap in 2D noise space.
     * @param w width of bitmap in 2D noise space.
     * @param h height of bitmap in 2D noise space.
     */
    public NoiseGenerator(NoiseFunction func, int octaves, float x, 
            float y, float w, float h) {
        if (octaves < 1 || octaves > 32) {
            throw new IllegalArgumentException("Invalid number of octaves.");
        }
        mFunc = func;
        mOctaves = octaves;
        mX = x;
        mY = y;
        mW = w;
        mH = h;
    }
    
    public NoiseGenerator(NoiseFunction func, float w, float h) {
        this(func, 4, 0, 0, w, h);
    }
    
    @Override
    public boolean init(TextureSize size) {
        mStepX = mW / size.value();
        mStepY = mH / size.value();
        return true;
    }

    @Override
    public float getPixel(Channel c, int x, int y) {
        final float xx = x*mStepX;
        final float yy = y*mStepY;
        
        float val = 0;
        switch (mFunc) {
        case PLAIN:
            val = SimplexNoise.noise2D(xx + mX, yy + mY);
            break;
            
        case PERLIN_A:
            for (int i = 0; i < mOctaves; i++) {
                final float f = Geometry.negPow2(i);
                final int ii = i+1;
                val += f*SimplexNoise.noise2D(ii*xx + mX, ii*yy + mY);
            }
            val /= 2;
            break;
            
        case PERLIN_B:
            for (int i = 0; i < mOctaves; i++) {
                final float f = Geometry.negPow2(i);
                final int ii = i+1;
                val += f*Math.abs(SimplexNoise.noise2D(ii*xx + mX, ii*yy + mY));
            }
            val /= 2;
            break;
            
        case PERLIN_C:
            for (int i = 0; i < mOctaves; i++) {
                final float f = Geometry.negPow2(i);
                final int ii = i+1;
                val += f*Math.abs(SimplexNoise.noise2D(ii*xx + mX, ii*yy + mY));
            }
            val = (float) Math.sin(xx+mX + val);
            break;            
        }
       
        // at this point noise value is in [-1..1], convert to [0..1]
        return (val+1)/2;
    }
    
    public enum NoiseFunction {
        /** 
         * Plain noise value. For this function the octave parameter will have
         * not effect!
         */
        PLAIN,
        /**
         * Perlin noise:  
         * val = noise(x) + 1/2*noise(2x) + 1/4*noise(4x) ...
         * Number of octaves can be adjusted via octave parameter.
         */
        PERLIN_A,
        /**
         * Perlin noise:  
         * val = |noise(x)| + 1/2*|noise(2x)| + 1/4*|noise(4x)| ...
         * Number of octaves can be adjusted via octave parameter. 
         */
        PERLIN_B,
        /**
         * Perlin noise:  
         * val = sin(x + |noise(x)| + 1/2*|noise(2x)| + 1/4*|noise(4x)| ... )
         * Number of octaves can be adjusted via octave parameter.      
         */
        PERLIN_C;
    }
}
