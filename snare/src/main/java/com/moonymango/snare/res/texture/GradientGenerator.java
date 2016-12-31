package com.moonymango.snare.res.texture;

import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource;

/**
 * Calculates color gradient based on specified color samples in a 
 * {@link ColorSamples} instance. Because samples comprise all channels,
 * the same instance of {@link GradientGenerator} may be used as source
 * for all channels.
 */
public class GradientGenerator implements ITextureChannelSource {
    
    private final ColorSamples mSamples;
    private final int mSamplesX;
    private final int mSamplesY;
    private float mFx;
    private float mFy;
        
    /**
     * Constructor. The content of the color channel is specified via a
     * matrix of color samples ({@link ColorSamples}). This matrix is mapped 
     * onto the bitmap and the 4 nearest sample values of a pixel are 
     * interpolated and added to get the final pixel value. Sample matrix is 
     * not required to be square.
     * </br></br>
     * Example:
     * color sample matrix
     * / a b c \
     * | d e f | => mapping on bitmap: a = top left corner, i = bottom right 
     * \ g h i /
     * In a 64x64 pixel bitmap the pixel with coordinates (62, 62) will be
     * somewhere in the lower right section, so it's value is an interpolation 
     * of samples e,f,h,i. 
     * 
     * @param Color samples matrix.
     */
    public GradientGenerator(ColorSamples samples) {
        mSamples = samples;
        mSamplesY = samples.getHeight();
        mSamplesX = samples.getWidth(); 
    }
    
    @Override
    public boolean init(TextureSize size) {
        final int s = size.value();
        if (s < mSamplesX || s < mSamplesY) {
            throw new IllegalStateException("Bitmap too small for number of " +
            		"specified samples.");
        }
        mFx = mSamplesX == 1 ? 0 : (float) (mSamplesX-1) / (s-1);
        mFy = mSamplesY == 1 ? 0 : (float) (mSamplesY-1) / (s-1);
        return true;
    }

    @Override
    public float getPixel(Channel c, int x, int y) {
        // get nearest samples to this pixel
        float sx = mFx*x;
        float sy = mFy*y;
        final int left = (int) sx;
        final int right = (left+1) % mSamplesX;
        final int top = (int) sy;
        final int bottom = (top+1) % mSamplesY;
        final float topLeft = mSamples.get(left, top, c);
        final float topRight = mSamples.get(right, top, c);
        final float bottomLeft = mSamples.get(left, bottom, c);
        final float bottomRight = mSamples.get(right, bottom, c);
                
        // interpolate
        sx -= left;
        sy -= top;
        final float ftl = (1-sx)*(1-sy);
        final float ftr = sx*(1-sy);
        final float fbl = (1-sx)*sy;
        final float fbr = sx*sy;
        return topLeft*ftl + topRight*ftr + bottomLeft*fbl + bottomRight*fbr;
    }
    
    /**
     * Array of color samples, which define interpolation points for
     * gradient generation. 
     */
    public static class ColorSamples {
        
        private final int mWidth;
        private final int mHeight;
        private final float[][][] mColor; 
        
        public ColorSamples(int width, int height) {
            if (width < 1 || height < 1) {
                throw new IllegalArgumentException("sample matrix must not be " +
                        "less than 1x1 elements");
            }
            mWidth = width;
            mHeight = height;
            
            // create array
            mColor = new float[width][][];
            for (int x = 0; x < width; x++) {
                mColor[x] = new float[height][];
                for (int y = 0; y < height; y++) {
                    mColor[x][y] = new float[4];
                }
            }
        }
        
        public int getWidth() {
            return mWidth;
        }
        
        public int getHeight() {
            return mHeight;
        }
        
        /**
         * Returns value for specified position and channel.
         * @param x
         * @param y
         * @param ch
         * @return
         */
        public float get(int x, int y, Channel ch) {
            switch (ch) {
            case R: return mColor[x][y][0];
            case G: return mColor[x][y][1];
            case B: return mColor[x][y][2];
            case A: return mColor[x][y][3];
            }
            return 0;
        }
        
        public ColorSamples set(int x, int y, float[] color) {
            final float[] c = mColor[x][y];
            for (int i = 0; i < 4; i++) {
                c[i] = color[i];
            }
            return this;
        }
        
        public ColorSamples set(int x, int y, float r, float g, 
                float b, float a) {
            final float[] c = mColor[x][y];
            c[0] = r;
            c[1] = g;
            c[2] = b;
            c[3] = a;
            return this;
        }
        
        public ColorSamples setAll(float[] color) {
            for (int x = 0; x < mWidth; x++) {
                for (int y = 0; y < mHeight; y++) {
                    set(x, y, color);
                }
            }
            return this;
        }
        
        public ColorSamples setAll(float r, float g, float b, float a) {
            for (int x = 0; x < mWidth; x++) {
                for (int y = 0; y < mHeight; y++) {
                    set(x, y, r, g, b, a);
                }
            }
            return this;
        }
        
        public ColorSamples setColumn(int x, float[] color) {
            for (int y = 0; y < mHeight; y++) {
                set(x, y, color);
            }
            return this;
        }
        
        public ColorSamples setColumn(int x, float r, float g, 
                float b, float a) {
            for (int y = 0; y < mHeight; y++) {
                set(x, y, r, g, b, a);
            }
            return this;
        }
        
        public ColorSamples setRow(int y, float[] color) {
            for (int x = 0; x < mWidth; x++) {
                set(x, y, color);
            }
            return this;
        }
        
        public ColorSamples setRow(int y, float r, float g, 
                float b, float a) {
            for (int x = 0; x < mWidth; x++) {
                set(x, y, r, g, b, a);
            }
            return this;
        }
    }
    
}
