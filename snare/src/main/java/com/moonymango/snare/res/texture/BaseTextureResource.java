package com.moonymango.snare.res.texture;

import android.content.res.Resources;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.BaseResource;
import com.moonymango.snare.res.IAssetName;

public abstract class BaseTextureResource extends BaseResource
{
    /** Texture region that covers the whole range (0, 0) to (1, 1) */
    public static final TextureRegion DEFAULT_TEXTURE_REGION = new TextureRegion(true, 0, 1, 1, 0);
    
    private final ITextureRegionProvider mProvider;
    
    public BaseTextureResource(IGame game, String name)
    {
        super(game, name);
        mProvider = null;
    }
    
    public BaseTextureResource(IGame game, String name, ITextureRegionProvider provider)
    {
        super(game, name);
        mProvider = provider;
    }
    
    public BaseTextureResource(IGame game, IAssetName asset)
    {
        super(game, asset);
        mProvider = null;
    }
    
    public BaseTextureResource(IGame game, IAssetName asset, ITextureRegionProvider provider)
    {
        super(game, asset);
        mProvider = provider;
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        return null;
    }
    
    public BaseTextureResHandle getHandle() {
        return (BaseTextureResHandle) getHandle(mGame.getResourceCache());
    }

    /**
     * @return
     */
    public TextureRegion getTextureRegion(String name) {
        if (mProvider != null && name != null) {
            return mProvider.getRegionByName(name);
        } 
        return DEFAULT_TEXTURE_REGION;
    }
   
    /**
     * Holds data of a region.
     */
    public static class TextureRegion {
        public final float LEFT;
        public final float RIGHT;
        public final float TOP;
        public final float BOTTOM;
        
        /**
         * Constructor.
         * Note: Images must normally be flipped to adapt the image coordinates 
         * to OpenGL texture coordinates. When the region is flipped here (flipY), 
         * then image files can be used as is. 
         *  
         * @param flipY Flips top and bottom for adaption to OpenGL texture coords.
         * @param left
         * @param right
         * @param top
         * @param bottom
         */
        public TextureRegion(boolean flipY, float left, float right, float top, float bottom) {
            LEFT = left;
            RIGHT = right;
            TOP = flipY ? bottom : top;
            BOTTOM = flipY ? top : bottom;
        }
    }
    
    public interface ITextureRegionProvider {
        TextureRegion getRegionByName(String name);
    }
}
