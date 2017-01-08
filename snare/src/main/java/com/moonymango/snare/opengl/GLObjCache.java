package com.moonymango.snare.opengl;

import com.moonymango.snare.opengl.BaseGLObj.GLObjState;
import com.moonymango.snare.util.Cache;

import java.util.ArrayList;

public class GLObjCache extends Cache<GLObjCache, GLObjDescriptor, BaseGLObj> {
    
    /** Stores GL objects in same order they were created. */
    private final ArrayList<BaseGLObj> mGLObjects = new ArrayList<BaseGLObj>();
    
    public GLObjCache() {
        super(CleanUpPolicy.IMMEDIATELY);
    }
    
    @Override
    protected boolean allowItemCreation(GLObjDescriptor descr) {
        // always allow new GL objects
        return true;
    }
    
    @Override
    protected void onItemCreated(BaseGLObj item) {
        mGLObjects.add(item);
    }
    
    @Override
    protected void onItemRemoved(BaseGLObj item) {
        mGLObjects.remove(item);
    }

    ////////////////////////////////////////////////////////////////
    // The following functions are intended to be called only from render 
    // thread! No further synchronization is needed since update
    // phase and render phase happen consecutively and cannot access 
    // this class at the same time.
    ////////////////////////////////////////////////////////////////    

    /**
     * This update function must be called prior to rendering from the
     * renderer's onDrawFrame to make sure everything is in the GPU.
     * 
     * Note: GL objects are loaded in LIFO-like order, i.e. objects
     * created last will be first to be loaded! 
     */
    void update() {
        for (int i = mGLObjects.size() - 1 ; i >= 0; i--) {
            BaseGLObj obj = mGLObjects.get(i);
            switch (obj.getState()) {
            case TO_LOAD:
                obj.onLoad();
                obj.setState(GLObjState.LOADED);
                break;
            case TO_UPDATE:
                obj.onUpdate();
                obj.setState(GLObjState.LOADED);
                break;
            case TO_UNLOAD:
                obj.onUnload();
                obj.setState(GLObjState.UNLOADED);
                // remove item manually here (see also BaseGLObj
                // onRemoveFromCachePending() override)
                dropItem(obj);
                break;
            default:
            }
        }   
    }
      
    /**
     * This is to be called from renderer's onSurfaceCreated.
     */
    void reloadAll() {
        // just mark all active gl objects to be loaded again 
        // during next call to update()
        // (use mRecentlyUsed because it is already an ArrayList
        //  of all known gl objects)
        int len = mGLObjects.size();
        for (int i = 0; i < len; i++) {
            BaseGLObj obj = mGLObjects.get(i);
            switch (obj.getState()) {
            case LOADED:
            case TO_UPDATE:
                obj.setState(GLObjState.TO_LOAD);
                break;
            default:
            }
        }   
    }
    
}
