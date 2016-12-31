package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;
import com.moonymango.snare.ui.scene3D.BaseEffect.RenderContext;
import com.moonymango.snare.ui.scene3D.Scene3D.DrawBundle;

/**
 * The drawable component manages all other components that affect rendering:
 * effects, mesh, material.
 * In onInit() it discovers all those components of the game object and
 * loads them to the GPU. It checks for compatibility of mesh and material
 * with the effects. 
 * It also creates a {@link DrawBundle} for each effect. The bundles
 * will be used by the scene to group everything by {@link RenderPass} and 
 * {@link RenderContext}. 
 */
public abstract class BaseSceneDrawable implements IComponent {

    protected GameObj mGameObj;
    protected boolean mIsVisible = true;
    protected boolean mIsInitialized;
    protected final RenderPass mRenderPass;
    protected boolean mIsFinished;
    protected BaseEffect[] mEffects = new BaseEffect[Scene3D.MAX_CTX_PER_DRAWABLE];
    protected int mEffectCnt;
    protected BaseMesh mMesh;
    protected Material mMat;
    
    private DrawBundle[] mBundle = new DrawBundle[Scene3D.MAX_CTX_PER_DRAWABLE];

    public BaseSceneDrawable(RenderPass pass) {
        mRenderPass = pass;
        // create bundles
        for (int i = 0; i < mBundle.length; i++) {
            mBundle[i] = new DrawBundle();
        }
    }

    public void show(boolean enable) {
        mIsVisible = enable;
    }

    /**
     * Returns all bundles of the drawable. The array may be longer
     * than the actual number of bundles. Unused elements have their
     * drawable references set to null. 
     * All unused elements are at the end of the array, so when
     * iterating over it, the first unused element indicates that no more
     * used elements will come. 
     * @return Bundle array.
     */
    public DrawBundle[] getBundles() 
    {
        if (!mIsInitialized)
            throw new IllegalStateException("No render context information " +
            		"available (non-initialized drawable component)");
        
        return mBundle;
    }

    public float getDistanceToCamera(BaseCamera camera) {
        // TODO prioC: distance to camera for alpha pass sorting
        return 0;
    }

    /**
     * Draw it.
     * @param scene  
     */
    public abstract void draw(Scene3D scene, DrawBundle bundle);
    
    public void reset() {
        // reset one-time effects so that it can be played again.
        for (int i = 0; i < mEffects.length; i++) {
            if (mEffects[i] != null)
                mEffects[i].reset();                
        }
        mIsFinished = false;
    }

    /**
     * Gets the state of stateful drawables, e.g. explosions.
     * For stateless drawables return always false.
     * @return 
     */
    public boolean isFinished() {
        return mIsFinished;
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public void onTraverse(Object userData) {}

    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}

    public void onInit() {
        if (mIsInitialized)
            throw new IllegalStateException("Drawable is already initialized.");
        
        // discover and load mesh and material (optional)
        mMesh = (BaseMesh) mGameObj.getComponent(ComponentType.MESH);
        if (mMesh != null) mMesh.loadToGpu();            
        
        mMat = (Material) mGameObj.getComponent(ComponentType.MATERIAL);
        if (mMat != null)  mMat.loadToGpu();
        
        // discover effects and load to GPU
        mEffectCnt = mGameObj.getComponentCnt(ComponentType.EFFECT);
        if (mEffectCnt < 1 || mEffectCnt > mEffects.length)
            throw new IllegalStateException("Too many effects at game object " +
            		mGameObj.getName());
        for (int e = 0; e < mEffectCnt; e++) {
            mEffects[e] = (BaseEffect) mGameObj.getComponent(ComponentType.EFFECT, e);
            mEffects[e].loadToGpu();
            
            // set up draw bundle for the effect
            final RenderContext c = mEffects[e].getContext();
            mBundle[e].setData(this, e, c, mRenderPass, mMat);
            
            // check compatibility with mesh and material
            mEffects[e].check(mMesh, mMat);
        }
                    
        mIsInitialized = true;
        reset(); 
    }

    public void onShutdown() {
        // unload stuff from GPU
        if (!mIsInitialized)
            throw new IllegalStateException("Drawable not initialized.");
        
        for (int e = 0; e < mEffects.length; e++) {
            if (mEffects[e] != null)
                mEffects[e].unloadFromGpu();
            mEffects[e] = null;
        }
        if (mMesh != null) mMesh.unloadFromGpu();
        if (mMat != null)  mMat.unloadFromGpu();
        
        mMesh = null;
        mMat = null;
        
        for (int i = 0; i < mBundle.length; i++) {
            mBundle[i].clear();
        }
        mIsInitialized = false;
    }

    public ComponentType getComponentType() {
        return ComponentType.RENDERING;
    }

    public GameObj getGameObj() {
        return mGameObj;
    }

    public void setGameObj(GameObj obj) {
        mGameObj = obj;
    }
 
}