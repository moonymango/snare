package com.moonymango.snare.res.texture;

import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource;

/**
 * Channel source which returns a constant value for each pixel.
 */
public class ConstGenerator implements ITextureChannelSource {

    private final float mVal;
    
    public ConstGenerator(float val) {
        mVal = val;
    }
    
    @Override
    public boolean init(TextureSize size) {
        return true;
    }

    @Override
    public float getPixel(Channel c, int x, int y) {
        return mVal;
    }

}
