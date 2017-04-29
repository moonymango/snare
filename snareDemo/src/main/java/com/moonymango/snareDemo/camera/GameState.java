package com.moonymango.snareDemo.camera;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.game.logic.ObjPosTransition;
import com.moonymango.snare.game.logic.ObjRotationTransition;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.util.EasingProfile;
import com.moonymango.snareDemo.Asset;


class GameState extends BaseSnareClass implements IGameState, IGameStateLogic, IEventListener {
    
    private static final int NUM_REF_CAMS = 3;
    private static final int FOV = 40;
    
    // the actual moving camera interpolates between position and orientation
    // of some reference cameras stored in this array
    private PerspectiveCamera[] mRefCam = new PerspectiveCamera[NUM_REF_CAMS];
    
    // moving camera
    private int mActual;
    private PerspectiveCamera mCamM;
    
    // arrays holding transition processes
    ObjPosTransition mPosTrans[] = new ObjPosTransition[NUM_REF_CAMS];
    ObjRotationTransition mRotTrans[] = new ObjRotationTransition[NUM_REF_CAMS];

    public GameState(IGame game)
    {
        super(game);
    }

    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        Scene3D s = new Scene3D(mGame);
        mGame.getPrimaryView().pushScreenElement(s);
        
        // background grid and lights
        GameObj obj = new GameObj(mGame, "grid");
        obj.addComponent(new LinesEffect(mGame));
        obj.addComponent(new GridMesh(mGame, 50, 50));
        obj.addComponent(new SceneDrawable(mGame, RenderPass.ENVIRONMENT));
        Material mat = new Material(mGame);
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        mGame.addGameObj(obj);
              
        obj = new GameObj(mGame, "directional_light");
        obj.rotate(0, 1, 0, -120).rotate(1, 0, 0, -30);
        Light l = new Light(LightType.DIRECTIONAL);
        obj.addComponent(l);
        l.setColor(1, 0, 0, 0);
        mGame.addGameObj(obj);
        
        obj = new GameObj(mGame, "ambient_light");
        l = new Light(LightType.AMBIENT);
        obj.addComponent(l);
        l.setColor(0, 0, 0.5f, 0);
        mGame.addGameObj(obj);
   
        
        // 1st game object + reference camera
        mRefCam[0] = new PerspectiveCamera();
        obj = new GameObj(mGame, "cam0");
        obj.addComponent(mRefCam[0]).setPosition(9, -5, 6f);
        mRefCam[0].lookAt(2, -2, 2, 0, 1, 0).setFieldOfView(FOV);
        mGame.addGameObj(obj);
        
        obj = new GameObj(mGame, "cube0");
        obj.addComponent( new DiffuseLightingEffect(mGame));
        final MeshResource mr = new MeshResource(mGame, Asset.CUBE3DS_MESH);
        obj.addComponent(new Mesh(mr));
        obj.addComponent(new SceneDrawable(mGame, RenderPass.ALPHA))
            .setPosition(2, -2, 2f)
            .setScale(1, 1, 2);
        mat = new Material(mGame);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        mGame.addGameObj(obj);
        
        // 2nd game object + reference camera
        mRefCam[1] = new PerspectiveCamera();
        obj = new GameObj(mGame, "cam1");
        obj.addComponent(mRefCam[1]).setPosition(-7, 5, 8);
        mRefCam[1].lookAt(-1, 0, 2, 1, 0, 0).setFieldOfView(FOV);
        mGame.addGameObj(obj);
        
        obj = new GameObj(mGame, "cube1");
        obj.addComponent(new DiffuseLightingEffect(mGame));
        obj.addComponent(new Mesh(mr));
        obj.addComponent(new SceneDrawable(mGame, RenderPass.ALPHA))
            .setPosition(-2, 0, 2f)
            .setScale(1, 2, 1);
        mat = new Material(mGame);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        mGame.addGameObj(obj);
        
        // 3rd game object + reference camera
        mRefCam[2] = new PerspectiveCamera();
        obj = new GameObj(mGame, "cam2");
        obj.addComponent(mRefCam[2]).setPosition(1, 4, -6);
        mRefCam[2].lookAt(0, 1, -2, 0, 1, 0).setFieldOfView(FOV);
        mGame.addGameObj(obj);
        
        obj = new GameObj(mGame, "cube2");
        obj.addComponent(new DiffuseLightingEffect(mGame));
        final MeshResource mrr = new MeshResource(mGame, Asset.SPHERE_MESH);
        obj.addComponent(new Mesh(mrr));
        obj.addComponent(new SceneDrawable(mGame, RenderPass.ALPHA))
            .setPosition(0, 1, -2)
            .setScale(0.5f);
        mat = new Material(mGame);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        mGame.addGameObj(obj);
        
        // place moving camera initially some distance away from the objects
        // to let it fly in on startup
        mCamM = new PerspectiveCamera();
        obj = new GameObj(mGame, "camM");
        obj.addComponent(mCamM).setPosition(25, 0, 0);
        mCamM.setFieldOfView(FOV).lookAt(0, 0, 0, 0, -1, 0);
        mGame.addGameObj(obj);
        s.setCamera(mCamM);
        
        // processes for camera transitions
        for (int i = 0; i < NUM_REF_CAMS; i++) {
            mPosTrans[i] = new ObjPosTransition(mCamM.getGameObj(), 
                    mRefCam[i].getGameObj(), 6000, EasingProfile.SQUARE_IN);
            
            mRotTrans[i] = new ObjRotationTransition(mCamM.getGameObj(), 
                    mRefCam[i].getGameObj(), 6000, EasingProfile.SQUARE_IN);
        }
        
        // start transition to make camera fly to the first reference position 
        mActual = 0;
        mPosTrans[mActual].run();
        mRotTrans[mActual].run();
        
        mGame.getEventManager().addListener(ITouchEvent.EVENT_TYPE, this);     
        mGame.showToast("double-tap to move camera to another position");
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

    @Override
    public String getName() {
        return GameState.class.getName();
    }

    @Override
    public boolean handleEvent(IEvent event) {
        // double tap is only enabled event, no differentiation of
        // touch event necessary
        
        for (int i = 0; i < NUM_REF_CAMS; i++) {
            if (mPosTrans[i].isRunning()) {
                // do nothing if we are still in a transition
                return true;
            }
        }
        
        // set next camera position and start transition
        mActual = (mActual+1)%NUM_REF_CAMS;
        mPosTrans[mActual].run();
        mRotTrans[mActual].run();
        
        return true;
    }
    
}

