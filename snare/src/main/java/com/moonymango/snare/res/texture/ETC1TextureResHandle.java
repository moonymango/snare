package com.moonymango.snare.res.texture;

import static android.opengl.GLES20.GL_RGB;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT_5_6_5;
import static com.moonymango.snare.opengl.GLES20Trace.glBindTexture;
import static com.moonymango.snare.opengl.GLES20Trace.glGenTextures;
import com.moonymango.snare.opengl.TextureObjOptions;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;

public class ETC1TextureResHandle extends BaseTextureResHandle {

    private final ETC1Texture mContent;
    
    public ETC1TextureResHandle(ETC1TextureResource res, ETC1Texture texture) {
        super(res);
        mContent = texture;
    }
    
    public ETC1Texture getContent() {
        return mContent;
    }

    @Override
    public int loadToGPU(TextureObjOptions options) {
        int textures[] = new int[1];
        int level = (options.mGenMipMap) ? 0 : options.mLevel;
        glGenTextures(1, textures, 0);
        glBindTexture(GL_TEXTURE_2D, textures[0]);
        ETC1Util.loadTexture(GL_TEXTURE_2D, level, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5, mContent);
        options.apply();
        glBindTexture(GL_TEXTURE_2D, 0);
        return textures[0];
    }
}
