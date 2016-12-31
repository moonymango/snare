package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.IMimic;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.VectorAF;

/**
 * Camera component for {@link GameObj}.
 * Note: {@link PerspectiveCamera} only takes the game objects position
 * for calculation of view transform. The rotation of the game object
 * is ignored.
 */
public class PerspectiveCamera extends BaseCamera implements IComponent,
        IMimic<PerspectiveCamera> {

    private float[] mViewTransformInv = new float[16];
    private boolean mViewInvOutOfDate = true;

    private float mFOV = Geometry.toRadian(45.0f);
    private int mScreenWidth = 1;
    private int mScreenHeight = 1;
    private final boolean mAutoScreenDimensions;
    private float mRatio = 1.0f;
    private float mNearWidth;
    private float mNearHeight;
    private float mNear = 1.0f;
    private float mFar = 100.0f;
    private boolean mUpdateProjection;
    private GameObj mGameObj;
    // init last update time to non-zero, so that very first update in
    // onPreDraw() at startup (time=0) is guaranteed
    private float mLastModTime  = -666;
    
    // tmp vectors to avoid allocations for intermediate results
    private final float[] mTmpVec1 = {1, 0, 0, 0};
    private final float[] mTmpVec2 = {1, 0, 0, 0};
    private final float[] mTmpVec3 = {1, 0, 0, 0};
       
    /**
     * Constructs camera with automatic screen dimensions.
     */
    public PerspectiveCamera() {
        this(true);
    }
    
    /**
     * 
     * @param autoScreenDimensions Set to true, if camera shall obtain screen
     *                       dimensions automatically. Dimensions cannot be
     *                       set manually in automatic mode.
     */
    public PerspectiveCamera(boolean autoScreenDimensions) {
        // init with neutral view transformation
        MatrixAF.setIdentityM(mViewTransform, 0);
        mAutoScreenDimensions = autoScreenDimensions;
    }
     
    public PerspectiveCamera setNearFarPlane(float near, float far) {
        mNear = near;
        mFar = far;
        mUpdateProjection = true;
        return this;
    }
    
    public float getNear() {return mNear;}
    public float getFar() {return mFar;}

    @Override
    protected void setScreenDimensions(int width, int height) {
        if (mAutoScreenDimensions) {
            throw new IllegalStateException("Setting screen dimensions " +
            		"manually is not allowed in auto mode.");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Dimensions must be greater than 0.");
        }
        mScreenWidth = width;
        mScreenHeight = height;
        mRatio = (float) width / height;
        mUpdateProjection = true;        
    }
    
    @Override
    public float[] getPosition() {
         return getGameObj().getPosition();
    }
    
    /**
     * Calculates viewing direction. This is done for each call.
     * @return
     */
    public float[] getViewDirection() {
        final float[] v = getGameObj().getForwardVector();
        for (int i = 0; i < 4; i++) {
            mTmpVec1[i] = v[i];
        }
        return mTmpVec1;
    }

    @Override
    public float[] getRayDirection(int x, int y) {
        if (x < 0 || x > mScreenWidth || y < 0 || y > mScreenHeight) {
            throw new IllegalArgumentException("Coordinates not in field of view.");
        }
        
        // update transformations
        onPreDraw();
        
        // project point on screen onto near plane in camera view space
        mTmpVec1[0] = mNearWidth * ((float)x * (1.0f/mScreenWidth) - 0.5f);
        mTmpVec1[1] = mNearHeight * (y * (1.0f/mScreenHeight) - 0.5f);
        mTmpVec1[2] = -mNear;
        mTmpVec1[3] = 1;
        
        // convert point back to world space and get direction based on camera position
        if (mViewInvOutOfDate) {
            MatrixAF.invertM(mViewTransformInv, 0, mViewTransform, 0);
            mViewInvOutOfDate = false;
        }
        MatrixAF.multiplyMV(mTmpVec2, 0, mViewTransformInv, 0, mTmpVec1, 0);         
        VectorAF.subtract(mTmpVec1, mTmpVec2, getGameObj().getPosition());
        VectorAF.normalize(mTmpVec1);
        
        return mTmpVec1;
    }

    /**
     * 
     * @param angle in degrees
     * @return
     */
    public PerspectiveCamera setFieldOfView(float angle) {
        mFOV = Geometry.toRadian(angle);
        mUpdateProjection = true;
        return this;
    }
    
    /**
     * 
     * @return angle in degrees
     */
    public float getFieldOfView() {
        return Geometry.toDegree(mFOV);
    }
    
    /**
     * Convenience function to calculate the camera's game object rotation
     * based on the actual position.
     * Note: Changing the obj position after that will not change the
     * viewing direction and the camera no longer points to the same point
     * @param x 
     * @param y
     * @param z
     * @param upX Up vector x.
     * @param upY Up vector y.
     * @param upZ Up vector z. 
     */
    public PerspectiveCamera lookAt(float x, float y, float z, float upX, float upY, 
            float upZ) {
        final GameObj obj = getGameObj();
        final float[] pos = obj.getPosition();
        // forward vector
        mTmpVec1[0] = x - pos[0];
        mTmpVec1[1] = y - pos[1];
        mTmpVec1[2] = z - pos[2];
        mTmpVec1[3] = 0;
        VectorAF.normalize(mTmpVec1);
        
        // left vector
        mTmpVec3[0] = upX;
        mTmpVec3[1] = upY;
        mTmpVec3[2] = upZ;
        mTmpVec3[3] = 0;
        VectorAF.cross(mTmpVec2, mTmpVec3, mTmpVec1);
        VectorAF.normalize(mTmpVec2);
        
        // up vector
        VectorAF.cross(mTmpVec3, mTmpVec1, mTmpVec2);
        VectorAF.normalize(mTmpVec3);
        
        obj.setRotation(mTmpVec2, mTmpVec3, mTmpVec1);
        return this;
    }
    
    // update all transformations based on parent game object position 
    // and orientation 
    public void onPreDraw() {  
        final GameObj obj = getGameObj();
        // compare time of last modification to check if view transform update
        // is needed
        final float t = obj.getLastModTime();
        final boolean objChanged = t != mLastModTime; 
        if (objChanged) {
            final float[] pos = obj.getPosition();
            final float[] forward = obj.getForwardVector();
            for (int i = 0; i < 4; i++) {
                // look-at point is current position + forward vector 
                mTmpVec1[i] = pos[i] + forward[i];
            }
            final float[] up = obj.getUpVector();
            MatrixAF.setLookAtM(mViewTransform, 0, pos[0], pos[1], pos[2], 
                    mTmpVec1[0], mTmpVec1[1], mTmpVec1[2], 
                    up[0], up[1], up[2]);
            
            mViewInvOutOfDate = true;
            mLastModTime = t;
        }
        
        if (mUpdateProjection) {
            final float w = mNear * (float) Math.tan(mFOV/2);
            final float h = w / mRatio; 
            mNearWidth = 2 * w;
            mNearHeight = 2* h; 
            MatrixAF.frustumM(mProjectionTransform, 0, -w, w, -h, h, mNear, mFar);
        }
        
        if (mUpdateProjection || objChanged) {
            MatrixAF.multiplyMM(mViewProjTransform, 0, mProjectionTransform, 0, mViewTransform, 0);
            mUpdateProjection = false;
        }
    }

    @Override
    public boolean mimic(PerspectiveCamera original) {
        mFOV                = original.mFOV;
        mNear               = original.mNear;
        mFar                = original.mFar;
        
        // Just copy screen dimension related data only when not in auto mode, 
        // because in auto mode the dimensions are taken from the 
        // game view to which the scene is attached.
        if (mAutoScreenDimensions) {
            mUpdateProjection = true;
            return true;
        }  
        mRatio              = original.mRatio;
        mScreenWidth        = original.mScreenWidth;
        mScreenHeight       = original.mScreenHeight;
        mUpdateProjection   = original.mUpdateProjection;
        if (!mUpdateProjection) {
            for (int i = 0; i < 16; i++) {
                mProjectionTransform[i] = original.mProjectionTransform[i];
            }
        }
        return true;
    }

    public void onAttachToScene(Scene3D scene) {
        final PlayerGameView view = scene.getView();
        if (mAutoScreenDimensions && view != null) {
            mScreenWidth = view.getScreenWidth();
            mScreenHeight = view.getScreenHeight();
            mRatio = (float) mScreenWidth / mScreenHeight;
            mUpdateProjection = true;        
        }
    }
    
    public void onDetachFromScene(Scene3D scene) {}

    @Override
    public ComponentType getComponentType() {return ComponentType.CAMERA;}
        
    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}

    @Override
    public void onTraverse(Object userData) {}

    @Override
    public void onInit() {}

    @Override
    public void onShutdown() {}

    @Override
    public void reset() {}

    @Override
    public GameObj getGameObj() {return mGameObj;}
    
    @Override
    public void setGameObj(GameObj obj) {mGameObj = obj;}

    
}
