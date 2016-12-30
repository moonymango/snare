package stachelsau.snare.opengl;

import static android.opengl.GLES20.*;
import stachelsau.snare.game.Game;
import stachelsau.snare.opengl.GLObjDescriptor.GLObjType;
import stachelsau.snare.res.texture.BaseTextureResHandle;
import stachelsau.snare.res.texture.BaseTextureResource;


public class TextureObj extends BaseGLObj  {
    
    private BaseTextureResource mTextureResource;
    private BaseTextureResHandle mTextureResHandle;
    private TextureObjOptions mOptions;
    private TextureSize mSize;
    
    protected TextureObj(GLObjDescriptor descriptor) {
        super(descriptor);
    }
    
    public void configure(BaseTextureResource texture, TextureObjOptions options) {
        if (isConfigured()) {
            return;
        }
        if (texture == null) {
            throw new IllegalArgumentException("Missing texture resource.");
        }
        mTextureResource = texture;
        mOptions = options != null ? options : 
                Game.get().getSettings().mDefaultTextureOptions;
        
        setState(GLObjState.TO_LOAD);
    }
    
    public void configure(BaseTextureResource texture) {
        configure(texture, null);
    }

    /** Configures an empty RGB565 texture */
    public void configure(TextureSize size, TextureObjOptions options) {
        if (isConfigured()) {
            throw new IllegalStateException("Already configured.");
        }
        final int maxSize = Game.get().getRenderer().getInfo().getMaxTextureSize();
        if (size.value() > maxSize) {
            throw new IllegalStateException("Unsupported texture size.");
        }
        mSize = size;
        mOptions = options != null ? options : 
            Game.get().getSettings().mDefaultTextureOptions;
        
        setState(GLObjState.TO_LOAD);
    }
    
    @Override
    public void onLoad() {
        if (mSize != null) {
            // create empty texture object
            if (mID == INVALID_ID) {
                int[] b = new int[1];
                glGenTextures(1, b, 0);
                mID = b[0];
            }  
            glBindTexture(GL_TEXTURE_2D, mID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, mSize.value(), 
                    mSize.value(), 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, null);
            checkGLError();
            mOptions.apply();
            glBindTexture(GL_TEXTURE_2D, 0);
            
        } else {
            // create texture from resource
            mTextureResHandle = (BaseTextureResHandle) mTextureResource.getHandle();
            mID = mTextureResHandle.loadToGPU(mOptions);
            checkGLError();
            mTextureResource.releaseHandle(mTextureResHandle);
            mTextureResHandle = null;
        }
    }

    @Override
    public void onUnload() {
        glBindTexture(GL_TEXTURE_2D, 0);
        int[] t = {mID};
        glDeleteTextures(1, t, 0); 
        mID = INVALID_ID;
    }
    
    /**
     * Binds to specified target of specified unit.
     * @param unit
     */
    public void bindToTextureUnit(int unit, int target) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(target, mID);
    }
    
    /**
     * Binds to GL_TEXTURE_2D of specified unit.
     * @param unit
     */
    public void bindToTextureUnit(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, mID);
    }

    /** 
     * Square texture sizes.
     */
    public enum TextureSize {
        S_2     (2),
        S_4     (4),
        S_8     (8),
        S_16    (16),
        S_32    (32),
        S_64    (64),
        S_128   (128),
        S_256   (256),
        S_512   (512),
        S_1024  (1024),
        S_2048  (2048);
        
        private static TextureSize[] sValues = TextureSize.values();
        /**
         * Find texture size which is adequate for given dimensions.
         * @param width
         * @param height
         * @return
         */
        public static TextureSize fit(int width, int height) {
            if (width < 1 || height < 1) {
                return null;
            }
            
            final int len = sValues.length;
            TextureSize sizeW = null;
            TextureSize sizeH = null;
            
            for (int i = 0; i < len; i++) {
                if (sValues[i].value() >= width) {
                    sizeW = sValues[i];
                    break;
                }
            }
            for (int i = 0; i < len; i++) {
                if (sValues[i].value() >= height) {
                    sizeH = sValues[i];
                    break;
                }
            }
            if (sizeW == null || sizeH == null) {
                // no fitting size available
                return null;
            }
            return TextureSize.max(sizeW, sizeH);
        }
        
        public static TextureSize max(TextureSize a, TextureSize b) {
            return a.value() >= b.value() ? a : b;
        }
        
        private final int mSize;
        
        private TextureSize(int size) {
            mSize = size;
        }
        
        public int value() {
            return mSize;
        }
    }
    
    /**
     * Contains information needed to bind texture to a texture unit.
     * Didn't know where else to put this helper class.
     */
    public static class TextureUnit {
        
        public final int unit;
        public final int target;
        
        // storage for texture object stuff
        public final BaseTextureResource res;
        public final TextureObjOptions options;
        private final GLObjDescriptor mTexDescr;
        private TextureObj mTexObj;
        
        public TextureUnit(int unit, int target, BaseTextureResource res,
                TextureObjOptions options)
        {
            this.unit = unit;
            this.target = target;
            this.res = res;
            this.options = options;
            mTexDescr = new GLObjDescriptor(res.getName(), GLObjType.TEXTURE);
        }
        
        public TextureUnit(int unit, BaseTextureResource res,
                TextureObjOptions options)
        {
            this(unit, GL_TEXTURE_2D, res, options);
        }
        
        public void load()
        {
            if (mTexObj == null)
            {
                mTexObj = (TextureObj) mTexDescr.getHandle();
                if (!mTexObj.isConfigured()) {
                    mTexObj.configure(res, options);
                }
            }
        }
        
        public void unload()
        {
            mTexDescr.releaseHandle(mTexObj);
            mTexObj = null;
        }
        
        public void bind()
        {
            mTexObj.bindToTextureUnit(unit);
        }
    }
}
