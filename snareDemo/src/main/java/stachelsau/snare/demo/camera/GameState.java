package stachelsau.snare.demo.camera;

import stachelsau.snare.demo.Asset;
import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.ITouchEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.IGameState;
import stachelsau.snare.game.IGameStateLogic;
import stachelsau.snare.game.logic.ObjPosTransition;
import stachelsau.snare.game.logic.ObjRotationTransition;
import stachelsau.snare.res.data.MeshResource;
import stachelsau.snare.ui.scene3D.Light;
import stachelsau.snare.ui.scene3D.Light.LightType;
import stachelsau.snare.ui.scene3D.RenderPass;
import stachelsau.snare.ui.scene3D.Material;
import stachelsau.snare.ui.scene3D.PerspectiveCamera;
import stachelsau.snare.ui.scene3D.Scene3D;
import stachelsau.snare.ui.scene3D.mesh.GridMesh;
import stachelsau.snare.ui.scene3D.mesh.Mesh;
import stachelsau.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import stachelsau.snare.ui.scene3D.rendering.LinesEffect;
import stachelsau.snare.ui.scene3D.rendering.SceneDrawable;
import stachelsau.snare.util.EasingProfile;


class GameState implements IGameState, IGameStateLogic, IEventListener {
    
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
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        Scene3D s = new Scene3D(); 
        Game.get().getPrimaryView().pushScreenElement(s);
        
        // background grid and lights
        GameObj obj = new GameObj("grid");
        obj.addComponent(new LinesEffect());
        obj.addComponent(new GridMesh(50, 50));
        obj.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
              
        obj = new GameObj("directional_light");
        obj.rotate(0, 1, 0, -120).rotate(1, 0, 0, -30);
        Light l = new Light(LightType.DIRECTIONAL);
        obj.addComponent(l);
        l.setColor(1, 0, 0, 0);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("ambient_light");
        l = new Light(LightType.AMBIENT);
        obj.addComponent(l);
        l.setColor(0, 0, 0.5f, 0);
        Game.get().addGameObj(obj);
   
        
        // 1st game object + reference camera
        mRefCam[0] = new PerspectiveCamera();
        obj = new GameObj("cam0");
        obj.addComponent(mRefCam[0]).setPosition(9, -5, 6f);
        mRefCam[0].lookAt(2, -2, 2, 0, 1, 0).setFieldOfView(FOV);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("cube0");
        obj.addComponent( new DiffuseLightingEffect());
        final MeshResource mr = new MeshResource(Asset.CUBE3DS_MESH);
        obj.addComponent(new Mesh(mr));
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA))
            .setPosition(2, -2, 2f)
            .setScale(1, 1, 2);
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
        
        // 2nd game object + reference camera
        mRefCam[1] = new PerspectiveCamera();
        obj = new GameObj("cam1");
        obj.addComponent(mRefCam[1]).setPosition(-7, 5, 8);
        mRefCam[1].lookAt(-1, 0, 2, 1, 0, 0).setFieldOfView(FOV);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("cube1");
        obj.addComponent(new DiffuseLightingEffect());
        obj.addComponent(new Mesh(mr));
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA))
            .setPosition(-2, 0, 2f)
            .setScale(1, 2, 1);
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
        
        // 3rd game object + reference camera
        mRefCam[2] = new PerspectiveCamera();
        obj = new GameObj("cam2");
        obj.addComponent(mRefCam[2]).setPosition(1, 4, -6);
        mRefCam[2].lookAt(0, 1, -2, 0, 1, 0).setFieldOfView(FOV);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("cube2");
        obj.addComponent(new DiffuseLightingEffect());
        final MeshResource mrr = new MeshResource(Asset.SPHERE_MESH);
        obj.addComponent(new Mesh(mrr));
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA))
            .setPosition(0, 1, -2)
            .setScale(0.5f);
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
        
        // place moving camera initially some distance away from the objects
        // to let it fly in on startup
        mCamM = new PerspectiveCamera();
        obj = new GameObj("camM");
        obj.addComponent(mCamM).setPosition(25, 0, 0);
        mCamM.setFieldOfView(FOV).lookAt(0, 0, 0, 0, -1, 0);
        Game.get().addGameObj(obj);
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
        
        Game.get().getEventManager().addListener(ITouchEvent.EVENT_TYPE, this);     
        Game.get().showToast("double-tap to move camera to another position");
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
    public void setNextState(IGameState next) {
                
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

