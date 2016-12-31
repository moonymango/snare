package com.moonymango.snareDemo.resolution;

import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IFlingEvent;
import com.moonymango.snare.events.IScaleEvent;
import com.moonymango.snare.events.IScrollEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.game.logic.RotationModifier;
import com.moonymango.snare.opengl.VarResolutionRenderer;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.util.VectorAF;

class GameState implements IGameState, IGameStateLogic,
        IEventListener {

    private PerspectiveCamera mCam;
    private GameObj mObj;
    private int mResX;
    private int mResY;
    private int mResXBeforeScale;
    private int mResYBeforeScale;
    private RotationModifier mProc;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        final VarResolutionRenderer r = (VarResolutionRenderer) Game.get().getRenderer();
        mResX = r.getMaxResolutionX();
        mResY = r.getMaxResolutionY();
        mResXBeforeScale = mResX;
        mResYBeforeScale = mResY;
        
        final Scene3D s = new Scene3D();
        
        mCam = new PerspectiveCamera();
        GameObj obj = new GameObj("camera");
        obj.addComponent(mCam);
        obj.setPosition(0, 0, 7);
        Game.get().addGameObj(obj);
        
        mCam.lookAt(0, 0, 0, 0, 1, 0);
        mCam.setNearFarPlane(2, 30);
        mCam.setFieldOfView(30);
        s.setCamera(mCam);
        
        obj = new GameObj("directionalLight");
        Light light = new Light(LightType.DIRECTIONAL);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        obj.rotate(0, 1, 0, 90);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("ambientLight");
        light = new Light(LightType.AMBIENT);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        Game.get().addGameObj(obj);
                    
        Game.get().getPrimaryView().pushScreenElement(s);
        
        
        MeshResource meshResSkull = new MeshResource(Asset.MONKEY3DS_MESH);
        mObj = new GameObj("mesh");
        //final SceneDrawableComponent c = new SceneDrawableComponent(new Mesh(meshResSkull), 
        //        new DiffuseLightingEffect(), RenderPass.DYNAMIC);
        mObj.addComponent(new DiffuseLightingEffect());
        mObj.addComponent(new Mesh(meshResSkull));
        mObj.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        final Material mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, 0, 0, 0.5f, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 0, 0, 1);
        mObj.addComponent(mat);
        //mObj.setScale(0.04f, 0.04f, 0.04f);
        mObj.setPosition(0, 0, 0);
        
        Game.get().addGameObj(mObj);
        
        // register for fling and scroll gestures
        final EventManager em = Game.get().getEventManager();
        em.addListener(IScrollEvent.EVENT_TYPE, this);
        em.addListener(IFlingEvent.EVENT_TYPE, this);
        em.addListener(ITouchEvent.EVENT_TYPE, this);
        em.addListener(IScaleEvent.EVENT_TYPE, this);
        
    }

    @Override
    public void onDeactivate(IGameState next) {
               
    }

    @Override
    public void onInit() {
        
    }

    @Override
    public void onShutdown() {
        
    }

    @Override
    public void setNextState(IGameState next) {
                
    }
    
    @Override
    public IGameStateLogic getGameStateLogic() {
        return this;
    }

    @Override
    public boolean equals(IGameState state) {
        return state == this;
    }

    public String getName() {
        return GameState.class.getName();
    }
    
    @Override
    public boolean handleEvent(IEvent event) {
        if (event.getType() == IScrollEvent.EVENT_TYPE) {
            handleScroll((IScrollEvent) event);
        }
        /*if (event.getType() == IFlingEvent.EVENT_TYPE) {
            handleFling((IFlingEvent) event);
        }*/
        if (event.getType() == IScaleEvent.EVENT_TYPE) {
            handleScale((IScaleEvent) event);
        }
        
        if (event.getType() == ITouchEvent.EVENT_TYPE) {
            final ITouchEvent e = (ITouchEvent) event;
            if (e.getTouchAction() == TouchAction.DOUBLE_TAB) {
                handleDoubleTap();
            }
        }
        return false;
    }
    
    private void handleScroll(IScrollEvent e) {
        if (mProc != null) {
            mProc.kill();
            mProc = null;
        }
        // rotate
        final float[] vec = {-e.getDistanceY(), -e.getDistanceX(), 0, 0};
        final float angle = 0.3f * VectorAF.normalize(vec);
        mObj.rotate(vec[0], vec[1], vec[2], angle);
    }
    
    private void handleDoubleTap() {
        if (mProc != null) {
            mProc.kill();
            mProc = null;
        }
        
        // reset to initial orientation
        mObj.resetRotation();
        
    }
    
    private void handleScale(IScaleEvent e) {
        // scale FOV between 1 and 60 degrees
        if (e.isNewScaleGesture()) {
            mResXBeforeScale = mResX;
            mResYBeforeScale = mResY;
        }
        
        final VarResolutionRenderer r = (VarResolutionRenderer) Game.get().getRenderer();
        final int tempX = mResX;
        final int tempY = mResY;
       
        mResX = (int) (mResXBeforeScale * e.getScaleFactor() * e.getScaleFactor());
        mResY = (int) (mResYBeforeScale * e.getScaleFactor() * e.getScaleFactor());
        
        if (mResY < 5 || mResY > r.getMaxResolutionY() 
                || mResX > r.getMaxResolutionX()) {
            mResX = tempX;
            mResY = tempY;
            return;
        }
        
        r.setResolution(mResX, mResY);
    }
    
}
