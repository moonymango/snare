package stachelsau.snare.res.texture;

import stachelsau.snare.opengl.TextureObj.TextureSize;
import stachelsau.snare.res.texture.BitmapTextureResource.ITextureChannelSource;

/**
 * Generates primitive shapes.
 * The getPixel() implementation delivers
 * always the same value, regardless of the requested channel. 
 */
public class ShapeGenerator implements ITextureChannelSource {

    private final Shape mShape;
    private final float mFeather;
    private final boolean mInvert;
    private int mORadius;
    private int mORadius2;
    private int mFRadius;
    private int mFRadius2;
    private int mDistOF;
    
    /**
     * 
     * @param shape Shape type to generate.
     * @param feather Add gradient on shape edges:
     *                1=no gradient;
     *                0=all shape gradient;
     * @param invert Invert values.
     */
    public ShapeGenerator(Shape shape, float feather, boolean invert) {
        if (feather < 0 || feather > 1) {
            throw new IllegalArgumentException("Invalid feather value.");
        }
        mFeather = feather;
        mShape = shape;
        mInvert = invert;
    }
    
    @Override
    public boolean init(TextureSize size) {
        switch (mShape) {
        case CIRCLE:
            mORadius = (size.value()-1)/2;
            mORadius2 = mORadius*mORadius;
            mFRadius = (int) (mORadius*mFeather);
            mFRadius2 = mFRadius*mFRadius;
            mDistOF = mORadius-mFRadius;
        }
        return true;
    }

    @Override
    public float getPixel(Channel c, int x, int y) {
        switch (mShape) {
        case CIRCLE:
            // get distance of pixel from center
            final int distX = Math.abs(x-mORadius);
            final int distY = Math.abs(y-mORadius);
            final int dist2 = distX*distX + distY*distY;
            if (dist2 > mORadius2) {
                // pixel is outside of circle 
                return mInvert ? 1 : 0;
            }
            if (dist2 < mFRadius2) {
                // pixel is inside feather radius
                return mInvert ? 0 : 1;
            }
            // pixel is in gradient, interpolate
            final int dist = (int) Math.sqrt(dist2);
            final float val = (float) (mORadius-dist) / mDistOF;      
            return mInvert ? 1-val : val;
        }
        return 0;
    }

    public enum Shape {
        CIRCLE
    }
}
