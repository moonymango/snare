package com.moonymango.snareDemo.scene;

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
import com.moonymango.snare.res.data.ImportTransformCenterToOrigin;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.OutlineEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.util.VectorAF;

class GameState implements IGameState, IGameStateLogic,
        IEventListener {

    private PerspectiveCamera mCam;
    private float mFOVbeforeScale = 30;
    private float mFOV = 30;
    private GameObj mObj;
    private RotationModifier mProc;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
    	// get skull's current position
        final float[] pos = mObj.getPosition();
        // change z coordinate, use delta time since last frame to get
        // smooth motion
        mObj.setPosition(pos[0], pos[1], pos[2] - virtualDelta*0.001f);
        
        // return null means we stay in this game state
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
    	
    	// startup: create the scene and objects
        final Scene3D s = new Scene3D();
        
        // +++ camera +++
        mCam = new PerspectiveCamera();
        GameObj obj = new GameObj("camera");
        obj.addComponent(mCam);
        obj.setPosition(0, 0, 7);
        Game.get().addGameObj(obj);
        
        mCam.lookAt(0, 0, 0, 0, 1, 0);
        mCam.setNearFarPlane(2, 30);
        mCam.setFieldOfView(mFOVbeforeScale);
        s.setCamera(mCam);
        
        // ++++ lights ++++
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
        
        // ++++ skull ++++
        // load skull shape from asset file
        MeshResource meshResSkull = new MeshResource(Asset.SKULL_MESH, 
                new ImportTransformCenterToOrigin(true));
        
        mObj = new GameObj("mesh");
        //final SceneDrawableComponent c = new SceneDrawableComponent(new Mesh(meshResSkull), 
        //        new DiffuseLightingEffect(), RenderPass.DYNAMIC);
        //mObj.addComponent(new CelShader());
        
        mObj.addComponent(new DiffuseLightingEffect()); 	// this effect draws the skull 
        mObj.addComponent(new OutlineEffect());				// this effect draws black outline
        mObj.addComponent(new Mesh(meshResSkull));
        mObj.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        final Material mat = new Material();
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 0, 0, 1);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 0, 0, 0.5f, 1);
        mat.setColor(Material.OUTLINE_COLOR_IDX, 0, 0, 0, 1);
        mObj.addComponent(mat);
        mObj.setScale(0.04f, 0.04f, 0.04f);				// set size of skull
        mObj.setPosition(0, 0, 0);						// put skull to origin
        
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
        if (event.getType() == IFlingEvent.EVENT_TYPE) {
            handleFling((IFlingEvent) event);
        }
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
    
    private void handleFling(IFlingEvent e) {
        if (mProc != null) {
            mProc.kill();
            mProc = null;
        }
        // let object rotate based on fling velocity
        final float[] vec = {e.getVelocityY(), e.getVelocityX(), 0, 0};
        final float angle = 0.3f * VectorAF.normalize(vec);
        mProc = new RotationModifier(mObj, vec[0], vec[1], vec[2], angle);
        mProc.run();
        
    }
    
    private void handleDoubleTap() {
        if (mProc != null) {
            mProc.kill();
            mProc = null;
        }
        
        // reset to initial orientation
        mObj.resetRotation();
        
        // reset FOV to initial value
        mFOV = 30;
        mCam.setFieldOfView(mFOV);
        
    }
    
    private void handleScale(IScaleEvent e) {
        // scale FOV between 1 and 60 degrees
        if (e.isNewScaleGesture()) {
            mFOVbeforeScale = mFOV;
        }
        mFOV = mFOVbeforeScale / e.getScaleFactor();
        mFOV = Math.min(60, Math.max(1, mFOV)); 
        mCam.setFieldOfView(mFOV);
    }

    
}
