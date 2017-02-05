package com.moonymango.snareDemo.physics;

import com.moonymango.snare.audio.AudioComponent;
import com.moonymango.snare.audio.SoundResource;
import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IUserEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.game.logic.ScrollLogicComponent;
import com.moonymango.snare.physics.BaseBoundingVolume;
import com.moonymango.snare.physics.BaseBoundingVolume.VolumeType;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.res.data.MeshResHandle;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.res.xml.BMFont;
import com.moonymango.snare.res.xml.BMFontXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.DiffuseLightingEffect;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.ui.widgets.BaseTouchWidget.TouchSetting;
import com.moonymango.snare.ui.widgets.BaseWidget.PositionAlignment;
import com.moonymango.snare.ui.widgets.Text;

class GameState implements IGameState, IGameStateLogic, IEventListener {

    private Text mAIScoreBoard;
    private Text mPlayerScoreBoard;
    private int mPlayerScore;
    private int mAIScore;
    
    public final static float GRID_Z = 12;
    public final static float GRID_X = 12;
    public final static float BACK = -GRID_Z/2;
    public final static float FRONT = GRID_Z/2;
    public final static float LEFT = -GRID_X/2;
    public final static float RIGHT = GRID_X/2;
    public final static float PADDLE_SIZE = 2;
    public final static float BORDER_WIDTH = 0.5f;
    public final static float BORDER_HEIGHT = 1;
    public final static float MIN_PADDLE_X = LEFT + PADDLE_SIZE/2;
    public final static float MAX_PADDLE_X = RIGHT - PADDLE_SIZE/2;
    public final static float[] AMBIENT_REFLECTION = {0, 0, 0.6f, 1};

    protected GameObj mBall;
    
    @Override
    public boolean handleEvent(IEvent event) {
        if (event.getType() == IUserEvent.EVENT_TYPE) {
            final IUserEvent e = (IUserEvent) event;
            if (e.getData() == PongObject.PLAYER_PADDLE) {
                // player missed ball, vibrate
                mAIScore++;
                mAIScoreBoard.setText(Integer.toString(mAIScore));
                Game.get().vibrate(200);
            } else {
                // ai missed the ball
                mPlayerScore++;
                mPlayerScoreBoard.setText(Integer.toString(mPlayerScore));
            }
        }
        return false;
    }

    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {

        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        
        final Scene3D scene = new Scene3D();
        
        // add camera
        final PerspectiveCamera cam = new PerspectiveCamera();
        GameObj obj = new GameObj("camera");
        obj.addComponent(cam);
        obj.setPosition(0, 6, 7);
        Game.get().addGameObj(obj);
        cam.lookAt(0, 0, 2.5f, 0, 0, -1);
        cam.setFieldOfView(100);
        
        scene.setCamera(cam);
        Game.get().getPrimaryView().pushScreenElement(scene);
        
        // lights
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
        
        // grid
        obj = new GameObj("grid");
        obj.addComponent(new LinesEffect(1));
        obj.addComponent(new GridMesh(50, 50));
        obj.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
           
        
        // score boards
        XMLResource<BMFont> mFontRes = 
                new XMLResource<BMFont>(Asset.IMPACT, new BMFontXMLHandler());
        XMLResHandle<BMFont> mFontHnd = mFontRes.getHandle();
        mPlayerScoreBoard = new Text(mFontHnd.getContent(), null, 
                PositionAlignment.CENTERED_XY, TouchSetting.NOT_TOUCHABLE, 5);
        mPlayerScoreBoard.setTextSize(200);
        final int x = (int) (Game.get().getPrimaryView().getScreenWidth() * 0.2f);
        final int y = (int) (Game.get().getPrimaryView().getScreenHeight() * 0.2f);
        mPlayerScoreBoard.setPosition(x, y);
        mPlayerScoreBoard.setColor(0.3f, 0.3f, 0.3f, 1);
        Game.get().getPrimaryView().pushScreenElement(mPlayerScoreBoard);
        
        mAIScoreBoard = new Text(mFontHnd.getContent(), null, 
                PositionAlignment.CENTERED_XY, null, 5);
        mAIScoreBoard.setTextSize(200);
        mAIScoreBoard.setPosition(4*x, 4*y);
        mAIScoreBoard.setColor(0.3f, 0.3f, 0.3f, 1);
        Game.get().getPrimaryView().pushScreenElement(mAIScoreBoard);
        
        
        
        final IPhysics p = Game.get().getPhysics();
        p.enableCollisionChecking(true);
        
        // --- ball ---
        MeshResource meshRes = new MeshResource(Asset.SPHERE_MESH);
        MeshResHandle meshHnd = meshRes.getHandle();
         
        final GameObj b = new GameObj(PongObject.BALL.name());
        b.enableEvents(false, false, true);
        b.addComponent(new DiffuseLightingEffect());
        b.addComponent(new Mesh(meshRes));
        b.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, AMBIENT_REFLECTION);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 0, 0, 1);
        b.addComponent(mat);
        
        BaseBoundingVolume bv = p.createBoundingVolume(VolumeType.SPHERE); 
        bv.setDimensions(meshHnd);
        b.addComponent(bv);
        
        b.addComponent(new BallLogic());
        
        b.addComponent(new AudioComponent(new SoundResource(Asset.DRIP_SOUND)));
        
        b.setScale(0.5f, 0.5f, 0.5f);
        Game.get().addGameObj(b);
        mBall = b;
        
        meshRes.releaseHandle(meshHnd);
        
         meshRes = new MeshResource(Asset.CUBE3DS_MESH);
         meshHnd = meshRes.getHandle();
        
        // --- left border ---
        final GameObj lb = new GameObj(PongObject.LEFT_BORDER.name());
        
        lb.addComponent(new DiffuseLightingEffect());
        lb.addComponent(new Mesh(meshRes)); 
        lb.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, AMBIENT_REFLECTION);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, AMBIENT_REFLECTION);
        lb.addComponent(mat);
        
         bv = p.createBoundingVolume(VolumeType.BOX); 
        bv.setDimensions(meshHnd);
        lb.addComponent(bv);
        
        lb.setScale(BORDER_WIDTH, BORDER_HEIGHT, GRID_Z - 1);
        lb.setPosition(LEFT, 0, 0);
        Game.get().addGameObj(lb);
        
        // --- right border ---
        final GameObj rb = new GameObj(PongObject.RIGHT_BORDER.name());
        
        rb.addComponent(new DiffuseLightingEffect());
        rb.addComponent(new Mesh(meshRes));
        rb.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, AMBIENT_REFLECTION);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, AMBIENT_REFLECTION);
        rb.addComponent(mat);
        
        bv = p.createBoundingVolume(VolumeType.BOX); 
        bv.setDimensions(meshHnd);
        rb.addComponent(bv);
        
        rb.setScale(BORDER_WIDTH, BORDER_HEIGHT, GRID_Z - 1);
        rb.setPosition(RIGHT, 0, 0);
        Game.get().addGameObj(rb);
        
        // --- players paddle ---
        final GameObj pp = new GameObj(PongObject.PLAYER_PADDLE.name());
        pp.addComponent(new DiffuseLightingEffect());
        pp.addComponent(new Mesh(meshRes));
        pp.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, AMBIENT_REFLECTION);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, AMBIENT_REFLECTION);
        pp.addComponent(mat);
        
        bv = p.createBoundingVolume(VolumeType.BOX); 
        bv.setDimensions(meshHnd);
        pp.addComponent(bv);
         
        pp.addComponent(new ScrollLogicComponent(new PlayerPaddleLogic()));
        
        pp.setScale(PADDLE_SIZE, BORDER_HEIGHT, BORDER_WIDTH);
        pp.setPosition(0, 0, FRONT);
        Game.get().addGameObj(pp);
        
        // --- ai paddle ---
        final GameObj ap = new GameObj(PongObject.AI_PADDLE.name());
        ap.addComponent(new DiffuseLightingEffect());
        ap.addComponent(new Mesh(meshRes));
        ap.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.AMBIENT_COLOR_IDX, AMBIENT_REFLECTION);
        mat.setColor(Material.DIFFUSE_COLOR_IDX, AMBIENT_REFLECTION);
        ap.addComponent(mat);
        
        bv = p.createBoundingVolume(VolumeType.BOX); 
        bv.setDimensions(meshHnd);
        ap.addComponent(bv);
        
        ap.addComponent(new AIPaddleLogic());
        
        ap.setScale(PADDLE_SIZE, BORDER_HEIGHT, BORDER_WIDTH);
        ap.setPosition(0, 0, BACK);
        Game.get().addGameObj(ap);
        
        
        meshRes.releaseHandle(meshHnd);  // release handle to cube
        
        
        
        Game.get().getEventManager().addListener(IUserEvent.EVENT_TYPE, this);
        
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
    
    public static enum PongObject {
        LEFT_BORDER,
        RIGHT_BORDER,
        PLAYER_PADDLE,
        AI_PADDLE,
        BALL
    }
    
}
