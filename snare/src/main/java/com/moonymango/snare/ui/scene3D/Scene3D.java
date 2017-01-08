package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjDestroyEvent;
import com.moonymango.snare.events.IGameObjNewEvent;
import com.moonymango.snare.events.IGameObjTouchEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.GameObjLayer;
import com.moonymango.snare.physics.Raycast;
import com.moonymango.snare.ui.IScreenElement;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.scene3D.BaseEffect.RenderContext;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.MatrixStack;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 3D scene. When the scene is attached to a view it listens to 
 * {@link IGameObjNewEvent} and {@link IGameObjDestroyEvent} events 
 * and adds or removes relevant components of the object automatically, i.e.
 * drawables and lights.
 * 
 * The scene manages the drawables in sorted lists, one for each render 
 * pass. During draw() those lists are iterated and the drawables gets
 * drawn. The drawables are sorted by render context and material. During
 * draw iteration the scene checks if a drawable belongs to a new render
 * context or material and performs a render context switch or binds the
 * necessary textures.
 */
public class Scene3D implements IScreenElement, IEventListener {
    
    /** Max. number of render contexts per drawable. */
    public static final int MAX_CTX_PER_DRAWABLE = 3;
    
    private PlayerGameView mView;
    private boolean mIsAttached;
    private BaseCamera mCamera;
    private final ArrayList<Light> mAmbientLights = new ArrayList<Light>();
    private final ArrayList<Light> mDirectionalLights = new ArrayList<Light>();
    private final ArrayList<Light> mPointLights = new ArrayList<Light>();
    private final ArrayList<Light> mSpotLights = new ArrayList<Light>();
    // one separate list of draw bundles for each render pass
    private final ArrayList<ArrayList<DrawBundle>> mPasses = 
            new ArrayList<ArrayList<DrawBundle>>();
    private boolean[] mListDirty;
    
    private final MatrixStack mViewTransformStack;
    private final GameObjLayer mLayerMask;
        
    private final float[] mCompleteTransform = new float[16];
        
    /**
     * Constructs scene using default layer mask.
     */
    public Scene3D() {
        this(Game.get().getSettings().SCENE_OPTIONS.DEFAULT_LAYER_MASK,
                Game.get().getSettings().SCENE_OPTIONS.SCENE_MATRIX_STACK_SIZE);
    }
    
    /**
     * Constructs scene using provided layer mask. Only game obj covered by this 
     * mask will be added to the scene and be considered by touch event raycasts.  
     * @param layerMask
     */
    public Scene3D(GameObjLayer layerMask) {
        this(layerMask, Game.get().getSettings().SCENE_OPTIONS.SCENE_MATRIX_STACK_SIZE);
    }
    
    public Scene3D(GameObjLayer layerMask, int matrixStackSize) {
        mViewTransformStack = new MatrixStack(matrixStackSize);
        mLayerMask = layerMask;
        
        // create collections for drawables
        for (int i = 0; i < RenderPass.COUNT; i++) {
            mPasses.add(new ArrayList<DrawBundle>());
        }
        mListDirty = new boolean[RenderPass.COUNT];
    }
      
    public void setCamera(BaseCamera camera) {
        if (mCamera != null) {
            mCamera.onDetachFromScene(this);
        }
        if (camera == null) {
            return;
        }
        mCamera = camera;
        mCamera.onAttachToScene(this);
    }
    
    public BaseCamera getCamera() {
        return mCamera;
    }
    
    public PlayerGameView getView() {
        if (mIsAttached) {
            return mView;
        }
        return null;
    }
    
    /**
     * Calculates the complete view projection matrix, including local-to-world, world-to-eye
     * and projection transformations.
     * @return
     */
    public float[] getModelViewProjMatrix() { 
        MatrixAF.multiplyMM(mCompleteTransform, 0, mCamera.getProjectionTransform(), 0, mViewTransformStack.getProduct(), 0);
        return mCompleteTransform;
    }
    
    public MatrixStack getViewTransformStack() {
        return mViewTransformStack;
    }
    
    public void onAttachToScreen(PlayerGameView view, int screenWidth, int screenHeight) {
        mView = view;
        mIsAttached = true;
        if (mCamera != null) {
            mCamera.onAttachToScene(this);
        }
        
        for (int i = 0; i < Game.get().getGameObjCnt(); i++) {
            final GameObj obj = Game.get().getObjByListIdx(i);
            addObjComponents(obj);
        }
        
        final EventManager em = Game.get().getEventManager(); 
        em.addListener(IGameObjNewEvent.EVENT_TYPE, this);
        em.addListener(IGameObjDestroyEvent.EVENT_TYPE, this);
    }
    
    public void onDetachFromScreen() {
        for (int i = 0; i < Game.get().getGameObjCnt(); i++) {
            final GameObj obj = Game.get().getObjByListIdx(i);
            removeObjComponents(obj);
        }
        
        mView = null;
        mIsAttached = false;
        
        final EventManager em = Game.get().getEventManager();
        em.removeListener(IGameObjNewEvent.EVENT_TYPE, this);
        em.removeListener(IGameObjDestroyEvent.EVENT_TYPE, this);
    }

    /** Add rendering and/or light components of an object to the scene. */
    private void addObjComponents(GameObj obj) 
    {
        if (obj == null || !mLayerMask.covers(obj.getLayer())) {
            return;
        }
        
        final BaseSceneDrawable d = (BaseSceneDrawable) 
                obj.getComponent(ComponentType.RENDERING);
        if (d != null) {
            final DrawBundle[] b = d.getBundles();
            final int len = Math.min(b.length, MAX_CTX_PER_DRAWABLE);
            for (int i = 0; i < len; i++) {
                if (b[i].mContext == null)
                    break;
                final int passIdx = b[i].mPass.ordinal(); 
                final ArrayList<DrawBundle> lst = 
                        mPasses.get(passIdx);
                lst.add(b[i]);    
                mListDirty[passIdx] = true;
            }
        }
        
        final Light l = (Light) obj.getComponent(ComponentType.LIGHT);
        if (l != null) {
            ArrayList<Light> lst = null;
            switch (l.getType()) {
            case AMBIENT:       lst = mAmbientLights; break;
            case DIRECTIONAL:   lst = mDirectionalLights; break;
            case POINT:         lst = mPointLights; break;
            case SPOT:          lst = mSpotLights; break;
            }
            lst.add(l);
            l.onAttachToScene();
        } 
    }
    
    /** Removes rendering and/or light components of an object to the scene. */
    private void removeObjComponents(GameObj obj) {
        if (obj == null || !mLayerMask.covers(obj.getLayer())) {
            return;
        }
        
        final BaseSceneDrawable d = (BaseSceneDrawable) 
                obj.getComponent(ComponentType.RENDERING);
        if (d != null) {
            final DrawBundle[] b = d.getBundles();
            for (int i = 0; i < b.length; i++) {
                if (b[i].mContext == null)
                   break;
                final ArrayList<DrawBundle> lst =
                        mPasses.get(b[i].mPass.ordinal());
                lst.remove(b[i]);
            }
        }
        
        final Light l = (Light) obj.getComponent(ComponentType.LIGHT);
        if (l != null) {
            ArrayList<Light> lst = null;
            switch (l.getType()) {
            case AMBIENT:       lst = mAmbientLights; break; 
            case DIRECTIONAL:   lst = mDirectionalLights; break;
            case POINT:         lst = mPointLights; break;
            case SPOT:          lst = mSpotLights; break; 
            }
            if (lst.remove(l)) {
                l.onDetachFromScene();
            }
        }
    }
    
    /** Returns the number of ambient lights in the scene. */
    public int getAmbientLightCnt() {
        return mAmbientLights.size();
    }
    
    /** Returns the number of directional lights in the scene. */
    public int getDirectionalLightCnt() {
        return mDirectionalLights.size();
    }
    
    /** Returns the number of point lights in the scene. */
    public int getPointLightCnt() {
        return mPointLights.size();
    }
    
    /** Returns the number of spot lights in the scene. */
    public int getSpotLightCnt() {
        return mSpotLights.size();
    }
    
    public Light getAmbientLight(int idx) {
        if (idx > mAmbientLights.size() - 1) {
            return null;
        }
        return mAmbientLights.get(idx);
    }
    
    public Light getDirectionalLight(int idx) {
        if (idx > mDirectionalLights.size() - 1) {
            return null;
        }
        return mDirectionalLights.get(idx);
    }
    
    public Light getPointLight(int idx) {
        if (idx > mPointLights.size() - 1) {
            return null;
        }
        return mPointLights.get(idx);
    }
    
    public Light getSpotLight(int idx) {
        if (idx > mSpotLights.size() - 1) {
            return null;
        }
        return mSpotLights.get(idx);
    }
    
    public void draw() {
        if (mCamera == null) {
            return;
        }
        
        mCamera.onPreDraw();  
        mViewTransformStack.reset();
        mViewTransformStack.pushMatrix(mCamera.getViewTransform());
        
        // TODO prioB: sort drawables by distance to camera 
        // (e.g. for correct alpha pass)
        RenderContext actualCtx = null;
        Material actualMat = null;
        for (int rp = 0; rp < RenderPass.COUNT; rp ++) 
        {
            final ArrayList<DrawBundle> l = mPasses.get(rp);
            // group drawables by render context and material if list is dirty
            if (mListDirty[rp]) {
                Collections.sort(l);
                mListDirty[rp] = false;
            }
            
            final int dcnt = l.size();
            for (int i = 0; i < dcnt; i++) {
                final DrawBundle db = l.get(i);
                if (!db.mDrawable.isVisible()) {
                    continue;
                }
                // check for render context change
                if (!db.mContext.equals(actualCtx)) {
                    if (actualCtx != null) {
                        actualCtx.end();
                    }
                    actualCtx = db.mContext;
                    actualCtx.begin();
                    //Log.e(Game.ENGINE_NAME, actualCtx.getName());
                }
                // check for material change
                if (db.mMaterial != null && !db.mMaterial.equals(actualMat)) {
                    actualMat = db.mMaterial;
                    actualMat.bindTextures();
                    //Log.e(Game.ENGINE_NAME, "mat change");
                }
                db.mDrawable.draw(this, db);
            }
        }
    }

    private boolean mIsVisible = true;
    
    public void show(boolean visible) {
        mIsVisible = visible;
    }
    
    public boolean isVisible() {
        return mIsVisible;
    }

    public boolean isAttached() {
        return mIsAttached;
    }

    public boolean onTouchEvent(ITouchEvent e) {
        // do a raycast to determine the touched game object
        final Game game = Game.get();
       
        final int x = e.getTouchX();
        final int y = e.getTouchY();
        final float[] s = mCamera.getPosition();
        final float[] v = mCamera.getRayDirection(x, y);
        
        final Raycast r = game.getPhysics().doRaycast(s, v, mLayerMask);
        final GameObj touchedObj = r.getNearestHit();
        
        if (touchedObj != null) {
            IGameObjTouchEvent te = game.getEventManager()
                    .obtain(IGameObjTouchEvent.EVENT_TYPE);
            te.setGameObjData(touchedObj.getID(), r.getNearestHitPoint(), 
                    e.getTouchAction(), x, y);
            game.getEventManager().queueEvent(te);
        }     
        r.recycle(); 
        return touchedObj != null;
    }
    
    public boolean containsCoord(int x, int y) {
        // always true, scene covers whole screen
        // TODO prioC: this will not work in case when multiple views are on 
        // the screen (split screen)
        return true;
    }

    public final boolean handleEvent(IEvent event) {
        if (event.getType().equals(IGameObjNewEvent.EVENT_TYPE)) {
            final GameObj obj = ((IGameObjNewEvent) event).getGameObj();
            addObjComponents(obj);
            
        }
        
        if (event.getType().equals(IGameObjDestroyEvent.EVENT_TYPE)) {  
            final GameObj obj = ((IGameObjDestroyEvent) event).getGameObj();
            removeObjComponents(obj);
        }
        return false;
    }
    
    /**
     * Bundles all information necessary to manage the drawables in 
     * the scene.
     */
    public static class DrawBundle implements Comparable<DrawBundle>{
        private BaseSceneDrawable mDrawable;
        private int mOrdinal;
        private RenderContext mContext;
        private RenderPass mPass;
        private Material mMaterial;
        
        /**
         * @param drawable Reference to drawable component.
         * @param ordinal Ordinal of context.
         * @param context Render context.
         * @param pass Render pass.
         * @param mat Material, or null if not available
         */
        public void setData(BaseSceneDrawable drawable, int ordinal, 
                RenderContext context, RenderPass pass, Material mat) {
            mDrawable = drawable;
            mOrdinal = ordinal;
            mContext = context;
            mPass = pass;
            mMaterial = mat;
        }
        
        public void clear() {
            mDrawable = null;
            mContext = null;
            mMaterial = null;
        }
        
        public RenderContext getContext() {return mContext;}
        public int getOrdinal() {return mOrdinal;}

        @Override
        public int compareTo(DrawBundle another) {
            if (mPass != another.mPass)
                throw new IllegalArgumentException("Cannot compare drawables " +
                        "of different render passes.");
    
            // context with lower ordinal is guaranteed to be rendered
            // before higher ordinal
            // 1. criterium ordinal
            int i = mOrdinal - another.mOrdinal;
            if (i != 0)
                return i;
            
            // 2. criterium: render context
            i = mContext.hashCode() - another.mContext.hashCode();
            if (i != 0)
                return i;
            
            // 3. criterium: material (optional)
            final int h0 = mMaterial != null ? mMaterial.hashCode() : 0;
            final int h1 = another.mMaterial != null ? another.mMaterial.hashCode() : 0;
            return h0 - h1;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result +
                    ((mDrawable == null) ? 0 : mDrawable.hashCode());
            result = prime * result + mOrdinal;
            return result;
        }

        /** 
         * Bundles are equal if they belong to same drawable and have same
         * ordinal. 
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DrawBundle other = (DrawBundle) obj;
            if (mDrawable == null) {
                if (other.mDrawable != null)
                    return false;
            } else if (!mDrawable.equals(other.mDrawable))
                return false;
            return mOrdinal == other.mOrdinal;
        }
        
        
        
    }
            
}
