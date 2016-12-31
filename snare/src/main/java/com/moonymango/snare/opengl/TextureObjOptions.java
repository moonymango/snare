package com.moonymango.snare.opengl;

import static android.opengl.GLES20.*;

public class TextureObjOptions {

    public static final TextureObjOptions NEAREST_CLAMP = 
            new TextureObjOptions(GL_NEAREST, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, false);
    
    public static final TextureObjOptions LINEAR_CLAMP = 
            new TextureObjOptions(GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE, false);
    
    public static final TextureObjOptions NEAREST_MIRRORED_REPEAT = 
            new TextureObjOptions(GL_NEAREST, GL_NEAREST, GL_MIRRORED_REPEAT, GL_MIRRORED_REPEAT, false);
    
    public static final TextureObjOptions LINEAR_MIRRORED_REPEAT = 
            new TextureObjOptions(GL_LINEAR, GL_LINEAR, GL_MIRRORED_REPEAT, GL_MIRRORED_REPEAT, false);
    
    public static final TextureObjOptions NEAREST_REPEAT = 
            new TextureObjOptions(GL_NEAREST, GL_NEAREST, GL_REPEAT, GL_REPEAT, false);
    
    public static final TextureObjOptions LINEAR_REPEAT = 
            new TextureObjOptions(GL_LINEAR, GL_LINEAR, GL_REPEAT, GL_REPEAT, false);
    
    public static final TextureObjOptions LINEAR_REPEAT_S_CLAMP_T = 
            new TextureObjOptions(GL_LINEAR, GL_LINEAR, GL_REPEAT, GL_CLAMP_TO_EDGE, false);
    
    public static final TextureObjOptions MIPMAP_NEAREST_REPEAT = 
            new TextureObjOptions(GL_NEAREST_MIPMAP_NEAREST, GL_NEAREST_MIPMAP_NEAREST, GL_REPEAT, GL_REPEAT, true);
    
    public static final TextureObjOptions MIPMAP_LINEAR_REPEAT = 
            new TextureObjOptions(GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR_MIPMAP_LINEAR, GL_REPEAT, GL_REPEAT, true);
    
    public final int mLevel = 0;
    public final int mMinFilter;
    public final int mMagFilter;
    public final int mWrapS;
    public final int mWrapT;
    public final boolean mGenMipMap; 
    
    private TextureObjOptions(int minFilter, int magFilter, int wrapS, int wrapT, boolean genMipMap) {
        mMinFilter = minFilter;
        mMagFilter = magFilter;
        mWrapS = wrapS;
        mWrapT = wrapT;
        mGenMipMap = genMipMap;
    }
    
    /** Apply options. Must be called in GL thread with texture bound to target */
    public void apply() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mMagFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mMinFilter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, mWrapS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, mWrapT);
        if (mGenMipMap) {
            // TODO prioC: mipmap generation
            //glGenerateMipmap(textures[0]);
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mLevel;
        result = prime * result + mMagFilter;
        result = prime * result + mMinFilter;
        result = prime * result + (mGenMipMap ? 1231 : 1237);
        result = prime * result + mWrapS;
        result = prime * result + mWrapT;
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TextureObjOptions other = (TextureObjOptions) obj;
        if (mLevel != other.mLevel)
            return false;
        if (mMagFilter != other.mMagFilter)
            return false;
        if (mMinFilter != other.mMinFilter)
            return false;
        if (mGenMipMap != other.mGenMipMap)
            return false;
        if (mWrapS != other.mWrapS)
            return false;
        if (mWrapT != other.mWrapT)
            return false;
        return true;
    }
    
}
