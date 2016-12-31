package com.moonymango.snare.opengl;

import static android.opengl.GLES20.GL_NO_ERROR;
import android.opengl.GLES20;
import com.moonymango.snare.util.CacheItem;

public abstract class BaseGLObj extends CacheItem<GLObjCache, GLObjDescriptor, BaseGLObj> {

    protected static final int INVALID_ID = -1;
    
    protected int mID = INVALID_ID;
    
    /**
     * State of an GL object.
     */
    public enum GLObjState {
        /** Object not ready to be loaded to GPU. */
        NOT_CONFIGURED,
        /** Object ready to be loaded to GPU. (onLoad() will be called) */
        TO_LOAD,
        /** Object is loaded to GPU. */
        LOADED,
        /** Object is loaded to GPU but needs to be updated (e.g.
         *  glBufferSubData). (onUpdate() will be called) 
         * */
        TO_UPDATE,
        /** Object is to be removed from GPU. */
        TO_UNLOAD,
        /** Object is removed from GPU. */
        UNLOADED;
    }
    
    private GLObjState mState = GLObjState.NOT_CONFIGURED;
    private GLObjState mPrevState = mState;
   
    
    public BaseGLObj(GLObjDescriptor descriptor) {
        super(descriptor);
        
    }
        
    public GLObjState getState() {
        return mState;
    }
    
    /**
     * Sets the GLObjects state, e.g. mark it to have it loaded to GPU by 
     * the render thread.   
     * @param state
     */
    public void setState(GLObjState state) {
        mPrevState = mState;
        mState = state;
        if (state == GLObjState.TO_LOAD) {
            mID = INVALID_ID;
        }
    }
    
    /**
     * Returns the GLObjects previous state.
     * @return
     */
    public GLObjState getPrevState() {
        return mPrevState;
    }
    
    /** 
     * Indicates whether the object was configured, so that it can be loaded
     * to GPU at next switch to GL thread. 
     * @return
     */
    public boolean isConfigured() {
        return mState != GLObjState.NOT_CONFIGURED;
    }
    
    /**
     * Indicates that object was loaded to GPU.
     * @return
     */
    public boolean isLoaded() {
        return mState == GLObjState.LOADED;
    }
    
    /**
     * Returns the GL id of the object, i.e. buffer or texture names.
     * @return
     */
    public int getID() {
        if (mState == GLObjState.NOT_CONFIGURED) {
            throw new IllegalStateException("GL object not configured.");
        }
        return mID;
    }
    
    /**
     * Loads the object's content to GPU. 
     */
    public abstract void onLoad();
    /**
     * Removes the content from the GPU.
     */
    public abstract void onUnload();
    /**
     * Updates the object's contents on GPU. 
     */
    public void onUpdate() {
        throw new UnsupportedOperationException("update not supported " +
        		"by this GL object.");
    };
    
 
    @Override
    public void onAddedToCache() {}
    @Override
    public void onRefCntDecr() {}
    
    @Override
    public final void onRefCntIncr() {
        // someone got a new reference to this object, so it must be loaded to
        // the GPU
        if (mState != GLObjState.LOADED && mState != GLObjState.NOT_CONFIGURED) {
            mState = GLObjState.TO_LOAD;
        }   
    }
    
    @Override
    public final boolean onRemoveFromCachePending() {
        // Because actual calls to GL must be done from the GL thread, the 
        // GLObjCache cannot use the built-in cleanup functions. We 
        // just mark the object for removal but keep it in the cache. On next
        // switch to GL thread it will be unloaded from GPU and removed from
        // cache.
        mState = GLObjState.TO_UNLOAD;
        return false;
    }
    
    protected void checkGLError() {
        int stat = GLES20.glGetError();
        if (stat != GL_NO_ERROR) {
            throw new IllegalStateException("GL error: 0x" 
                    + Integer.toHexString(stat) + " while loading " 
                    + mDescriptor.getQName());
        }
    }
    
}
