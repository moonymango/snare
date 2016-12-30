package stachelsau.snare.res.texture;

import stachelsau.snare.opengl.TextureObj.TextureSize;
import stachelsau.snare.res.texture.BitmapTextureResource.ITextureChannelSource;

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
