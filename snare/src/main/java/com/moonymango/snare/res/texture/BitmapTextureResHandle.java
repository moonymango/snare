package com.moonymango.snare.res.texture;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static com.moonymango.snare.opengl.GLES20Trace.glBindTexture;
import static com.moonymango.snare.opengl.GLES20Trace.glGenTextures;
import com.moonymango.snare.opengl.TextureObjOptions;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public class BitmapTextureResHandle extends BaseTextureResHandle {

    private final Bitmap mContent;
    
    public BitmapTextureResHandle(BitmapTextureResource res, Bitmap bitmap) {
        super(res);
        mContent = bitmap;
    }
    
    public Bitmap getContent() {
        return mContent;
    }

    @Override
    public boolean onRemoveFromCachePending() {
        mContent.recycle();
        return true;
    }

    @Override
    public int loadToGPU(TextureObjOptions options) {
        int textures[] = new int[1];
        int level = (options.mGenMipMap) ? 0 : options.mLevel;
        glGenTextures(1, textures, 0);
        glBindTexture(GL_TEXTURE_2D, textures[0]);
        GLUtils.texImage2D(GL_TEXTURE_2D, level, mContent, 0);
        options.apply();     
        glBindTexture(GL_TEXTURE_2D, 0);
        return textures[0];
    }
    
}
