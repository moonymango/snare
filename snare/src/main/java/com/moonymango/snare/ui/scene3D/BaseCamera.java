package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.util.VectorAF;

/**
 * Implements some helper functions to extract data from view and projection matrices. 
 */
public abstract class BaseCamera {
    
    public static final int FRUSTRUM_LEFT_PLANE_IDX = 0;
    public static final int FRUSTRUM_RIGHT_PLANE_IDX = 1;
    public static final int FRUSTRUM_TOP_PLANE_IDX = 2;
    public static final int FRUSTRUM_BOTTOM_PLANE_IDX = 3;
    public static final int FRUSTRUM_NEAR_PLANE_IDX = 4;
    public static final int FRUSTRUM_FAR_PLANE_IDX = 5;
    
    protected final float[] mViewTransform = new float[16];
    protected final float[] mProjectionTransform = new float[16];
    protected final float[] mViewProjTransform = new float[16];
    
    private final float[] mFrustrumPlane = new float[4];
    
    public float[] getViewTransform()           {return mViewTransform;}
    public float[] getProjectionTransform()     {return mProjectionTransform;}
    public float[] getViewProjectionTransform() {return mViewProjTransform;}
        
    /**
     * Sets dimensions of screen area that the camera should cover.
     * @param width
     * @param height
     */
    protected abstract void setScreenDimensions(int width, int height);
    /**
     * Returns the direction vector from camera's position to a
     * point on the near plane given by screen coordinates.
     * @param x Screen coordinate (origin = upper left corner)
     * @param y Screen coordinate (origin = upper left corner)
     * @return direction vector in world space
     */
    protected abstract float[] getRayDirection(int x, int y);
    /** Camera position in world coordinates */
    public abstract float[] getPosition();
    public abstract float[] getViewDirection();
    /** 
     * Called when camera becomes the active camera of a scene. and/or when
     * the scene gets attached to a view. This is intended to set the
     * screen dimensions (ratio) of the camera.  
     */
    protected abstract void onAttachToScene(Scene3D scene);
    /** Called when camera is no longer active camera of a scene. */
    protected abstract void onDetachFromScene(Scene3D scene);
    /** 
     * Called right before rendering of a scene starts. Update
     * view and projection matrix here.
     */
    protected abstract void onPreDraw();
        
    /**
     * Returns the distance of a point from one of the frustrum's panes 
     * @param p Point in world coordinates
     * @param plane Plane identified by an integer.
     * @return Distance (neg. values mean that p is not inside the frustrum)
     */
    public float getPlaneDistance(float[] p, int plane) {
        switch(plane) {
        case FRUSTRUM_LEFT_PLANE_IDX:
            // left pane: row 4 + row 1
            mFrustrumPlane[0] = mViewProjTransform[3] + mViewProjTransform[0];
            mFrustrumPlane[1] = mViewProjTransform[7] + mViewProjTransform[4];
            mFrustrumPlane[2] = mViewProjTransform[11] + mViewProjTransform[8];
            mFrustrumPlane[3] = mViewProjTransform[15] + mViewProjTransform[12];
            break;
               
        case FRUSTRUM_RIGHT_PLANE_IDX:
            // right pane: row 4 - row 1
            mFrustrumPlane[0] = mViewProjTransform[3] - mViewProjTransform[0];
            mFrustrumPlane[1] = mViewProjTransform[7] - mViewProjTransform[4];
            mFrustrumPlane[2] = mViewProjTransform[11] - mViewProjTransform[8];
            mFrustrumPlane[3] = mViewProjTransform[15] - mViewProjTransform[12];
            break;
                
        case FRUSTRUM_TOP_PLANE_IDX:
            // top pane: row 4 - row 2
            mFrustrumPlane[0] = mViewProjTransform[3] - mViewProjTransform[1];
            mFrustrumPlane[1] = mViewProjTransform[7] - mViewProjTransform[5];
            mFrustrumPlane[2] = mViewProjTransform[11] - mViewProjTransform[9];
            mFrustrumPlane[3] = mViewProjTransform[15] - mViewProjTransform[13];
            break;
            
        case FRUSTRUM_BOTTOM_PLANE_IDX:
            // bottom: row 4 + row 2
            mFrustrumPlane[0] = mViewProjTransform[3] + mViewProjTransform[1];
            mFrustrumPlane[1] = mViewProjTransform[7] + mViewProjTransform[5];
            mFrustrumPlane[2] = mViewProjTransform[11] + mViewProjTransform[9];
            mFrustrumPlane[3] = mViewProjTransform[15] + mViewProjTransform[13];
            break;
            
        case FRUSTRUM_NEAR_PLANE_IDX:
            // near: row 4 + row 3
            mFrustrumPlane[0] = mViewProjTransform[3] + mViewProjTransform[2];
            mFrustrumPlane[1] = mViewProjTransform[7] + mViewProjTransform[6];
            mFrustrumPlane[2] = mViewProjTransform[11] + mViewProjTransform[10];
            mFrustrumPlane[3] = mViewProjTransform[15] + mViewProjTransform[14];
            break; 
            
        case FRUSTRUM_FAR_PLANE_IDX:
            // far: row 4 - row 3
            mFrustrumPlane[0] = mViewProjTransform[3] - mViewProjTransform[2];
            mFrustrumPlane[1] = mViewProjTransform[7] - mViewProjTransform[6];
            mFrustrumPlane[2] = mViewProjTransform[11] - mViewProjTransform[10];
            mFrustrumPlane[3] = mViewProjTransform[15] - mViewProjTransform[14];
            break;
            
        default:
            throw new IllegalArgumentException("Invalid plane index.");
                
        }
        
        VectorAF.normalize(mFrustrumPlane);
        return VectorAF.dot(p, mFrustrumPlane);
        
    }

}