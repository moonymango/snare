package com.moonymango.snare.opengl;

import static android.opengl.GLES20.*;

import java.util.Arrays;

/**
 * Management of GL state (everything which is normally done via 
 * glEnable and glDisable) 
 * Usage before start drawing something:
 *      1. Enable the stuff you need.
 *      2. Invoke apply(). This is mandatory even nothing was enabled in step 1. 
 */
public class GLState {

    // global static variables keeping track of current state 
    private static boolean sDepth;
    private static boolean sDepthMask;
    private static boolean sDither;
    private static final boolean[] sColorMask = new boolean[4];
    private static boolean sBlend;
    private static int sSFactor;
    private static int sDFactor;
    private static boolean sCull;
    private static int sFace;
    private static int sShader;
   
    
    /** Synchronizes actual GL state and data in {@link GLState} class. */
    public static void sync() {
        sDepth = false;
        glDisable(GL_DEPTH_TEST);
        
        sDepthMask = true;
        glDepthMask(sDepthMask);
        sColorMask[0] = true;
        sColorMask[1] = true;
        sColorMask[2] = true;
        sColorMask[3] = true;
        glColorMask(sColorMask[0], sColorMask[1], sColorMask[2], sColorMask[3]);
        
        sDither = false;
        glDisable(GL_DITHER);
                
        sBlend = false;
        sSFactor = GL_ONE;
        sDFactor = GL_ONE;
        glBlendFunc(sSFactor, sDFactor);
        glDisable(GL_BLEND);
        
        sFace = GL_FRONT;
        sCull = false;
        glCullFace(sFace);
        glDisable(GL_CULL_FACE);
        glFrontFace(GL_CCW);
        
        sShader = -1;
        
    }
    
    private boolean mDepth;
    private boolean mDepthMask = true;
    private boolean mDither = true;
    private final boolean[] mColorMask = {true, true, true, true}; 
    private boolean mBlend;
    private int mSFactor;
    private int mDFactor;
    private boolean mCull;
    private int mFace;
    private int mHash;
    private boolean mLocked;
    
    
    
    public GLState() {}
    
    public GLState enableDepth() {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mDepth = true;
        return this;
    }
    
    public GLState setDepthMask(boolean enable) {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mDepthMask = enable;
        return this;
    }
    
    public GLState setColorMask(boolean r, boolean g, boolean b, boolean a)
    {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mColorMask[0] = r;
        mColorMask[1] = g;
        mColorMask[2] = b;
        mColorMask[3] = a;
        return this;
    }
    
    public GLState disableDithering() {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mDither = false;
        return this;
    }
    
    public GLState enableBlend(int sfactor, int dfactor) {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mBlend = true;
        mSFactor = sfactor;
        mDFactor = dfactor;
        return this;
    }
    
    /**
     * Front face is always GL_CCW.
     * @param face
     */
    public GLState enableFrontFaceCulling() {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mCull = true;
        mFace = GL_FRONT;
        return this;
    }
    
    /**
     * Front face is always GL_CCW.
     * @param face
     */
    public GLState enableBackFaceCulling() {
        if (mLocked)
            throw new IllegalStateException("Cannot modify locked state.");
        mCull = true;
        mFace = GL_BACK;
        return this;
    }
    
    /**
     * Apply previously defined state changes (see enable.. functions).
     * Calling this without having enabled anything will set GL state to
     * default (depth test, blending and face culling disabled)
     * 
     * @param program Shader to use.
     */
    public void apply(int program) {
        
        if (program != sShader) {
            glUseProgram(program);
            sShader = program;
        }
        
        if (mDepth != sDepth) {
            if (mDepth) {
                glEnable(GL_DEPTH_TEST);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
            sDepth = mDepth;
        }
        
        if (mDepthMask != sDepthMask) {
            glDepthMask(mDepthMask);
            sDepthMask = mDepthMask;
        }
        
        if (!Arrays.equals(mColorMask, sColorMask)) {
            glColorMask(mColorMask[0], mColorMask[1], mColorMask[2], mColorMask[3]);
            System.arraycopy(mColorMask, 0, sColorMask, 0, 4);
        }
        
        if (mDither != sDither) {
            if (mDither) {
                glEnable(GL_DITHER);
            } else {
                glDisable(GL_DITHER);
            }
            sDither = mDither;
        }
        
        if (mBlend) {
            if (mBlend != sBlend || mSFactor != sSFactor || 
                    mDFactor != sDFactor) {
                glBlendFunc(mSFactor, mDFactor);
                glEnable(GL_BLEND);
                sSFactor = mSFactor;
                sDFactor = mDFactor;
                sBlend = mBlend;
            }
        } else {
            if (mBlend != sBlend) {
                glDisable(GL_BLEND);
                sBlend = mBlend;
            }
        }
        
        if (mCull) {
            if (mCull != sCull || mFace != sFace) {
                glCullFace(mFace);
                glEnable(GL_CULL_FACE);
                sFace = mFace;
                sCull = mCull;
            }
        } else {
            if (mCull != sCull) {
                glDisable(GL_CULL_FACE);
            }
            sCull = mCull;
        }
    }

    /**
     * Locks state and calculates it's hash. After this the state cannot be
     * changed any more.
     * @return
     */
    public GLState lock() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (mBlend ? 1231 : 1237);
        result = prime * result + Arrays.hashCode(mColorMask);
        result = prime * result + (mCull ? 1231 : 1237);
        result = prime * result + mDFactor;
        result = prime * result + (mDepth ? 1231 : 1237);
        result = prime * result + (mDepthMask ? 1231 : 1237);
        result = prime * result + (mDither ? 1231 : 1237);
        result = prime * result + mFace;
        result = prime * result + mSFactor;
        mHash = result;
        mLocked = true;
        return this;
    }

    @Override
    public int hashCode() {
        if (!mLocked) 
            throw new IllegalStateException("Unlocked GlState cannot provide hash.");
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GLState other = (GLState) obj;
        if (mBlend != other.mBlend)
            return false;
        if (!Arrays.equals(mColorMask, other.mColorMask))
            return false;
        if (mCull != other.mCull)
            return false;
        if (mDFactor != other.mDFactor)
            return false;
        if (mDepth != other.mDepth)
            return false;
        if (mDepthMask != other.mDepthMask)
            return false;
        if (mDither != other.mDither)
            return false;
        if (mFace != other.mFace)
            return false;
        return mSFactor == other.mSFactor;
    }
    
    
}
