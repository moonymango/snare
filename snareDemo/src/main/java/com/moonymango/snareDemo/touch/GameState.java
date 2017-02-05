package com.moonymango.snareDemo.touch;

import com.moonymango.snare.audio.LoopResource;
import com.moonymango.snare.audio.PlayLoopAndFadeProc;
import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.ICameraMoveEvent;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.GameObjLayer;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.ETC1TextureResource;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.rendering.CircularShapeEmitter;
import com.moonymango.snare.ui.scene3D.rendering.FireAndForgetEffectProc;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.OffsetSceneDrawable;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.util.Pool;

class GameState implements IGameState, IGameStateLogic,
        IEventListener {
    
    private static final float[] MOTION_DIRECTION = {0, 0, -1, 0};
    /** Layer for monkeys */
    public static final GameObjLayer mMonkeyLayer = new GameObjLayer("monkeys", 0x1);
    /** Layer for all non-monkey game objects */
    public static final GameObjLayer mOthersLayer = new GameObjLayer("others", 0x2);
    
    private Scene3D mScene;
    private ExplosionPool mExplosions;
    private OffsetSceneDrawable mGrid;
    //private OffsetSceneDrawable mScoreBoard;
    private PerspectiveCamera mCamera;
    //private TextMesh mScoreText;
    private int mScore;
    
    private DemoObjPool mObjPool;
    private float mZPos;
    private int mXPos;
    
    private PlayLoopAndFadeProc mMusicProc;

    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
        // send camera position information 
        final EventManager em = Game.get().getEventManager();
        final ICameraMoveEvent e = (ICameraMoveEvent) em.obtain(ICameraMoveEvent.EVENT_TYPE);
        final float[] pos = mCamera.getPosition();
        e.setCamPosition(pos[0], pos[1], pos[2]);
        em.queueEvent(e);
        
        // add game objects randomly
        if (Game.get().getRandomInt(0, 100) < 5) {
            final GameObj obj = mObjPool.obtain();
            Game.get().addGameObj(obj);
            obj.setPosition(mXPos++, 0, mZPos - 100);
            mXPos = mXPos > 3 ? -3 : mXPos;
        }
        
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        mExplosions = new ExplosionPool();
        mScene = new Scene3D();
        Game.get().getPrimaryView().pushScreenElement(mScene);
        
        // add camera
        mCamera = new PerspectiveCamera();
        mCamera.setFieldOfView(30);
        mScene.setCamera(mCamera);
        GameObj obj = new GameObj("camera", mOthersLayer);
        obj.addComponent(mCamera);
        obj.setPosition(2, 2, 0);
        
        // add grid to camera's game object
        obj.addComponent(new LinesEffect());
        obj.addComponent(new GridMesh(7, 1000));
        mGrid = new OffsetSceneDrawable(RenderPass.ENVIRONMENT);
        mGrid.setOffet(-2, -2.5f, 0); // grid is centered as y=-0.5
        obj.addComponent(mGrid);
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
        
        // let camera move towards negative z direction
        final CameraOperatorProc p = new CameraOperatorProc(obj, 15, MOTION_DIRECTION);
        p.run();
        
        // lights
        obj = new GameObj("directionalLight", mOthersLayer);
        Light light = new Light(LightType.DIRECTIONAL);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        obj.rotate(0, 1, 0, 90);
        Game.get().addGameObj(obj);
        
        obj = new GameObj("ambientLight", mOthersLayer);
        light = new Light(LightType.AMBIENT);
        light.setColor(1, 1, 1, 0);
        obj.addComponent(light);
        Game.get().addGameObj(obj);
        
        // scoreboard
        /*XMLResource<BMFont> xmlRes = new XMLResource<BMFont>(Asset.IMPACT, new BMFontXMLHandler());
        XMLResHandle<BMFont> xmlHnd = xmlRes.getHandle();
        BMFont font = xmlHnd.getContent();
        xmlRes.releaseHandle(xmlHnd);
        
        mScoreText = new TextMesh(font, "Score: 0");
        mScoreBoard = new CameraBoundSceneDrawable(mScoreText, new PlainTextureEffect(), 
                RenderPass.ENVIRONMENT, mCamera);
        mScoreBoard.getMaterial().setAll(0.3f, 0.3f, 0.3f, 1);
        mScoreBoard.setOffet(-6f, -1.5f, -15);
        mScoreBoard.setRotation(0, 1, 0, 90);
        mScoreBoard.setScale(0.05f, 0.015f, 0.015f);
        obj = new GameObj("scoreboard", mOthersLayer);
        obj.addComponent(mScoreBoard);
        Game.get().addGameObj(obj);*/
        
        // register for catch events
        Game.get().getEventManager().addListener(IDemoObjCatchedEvent.EVENT_TYPE, this);
       
        // start music process
        final LoopResource lr = new LoopResource(Asset.SURESHOT_LOOP);
        mMusicProc = new PlayLoopAndFadeProc(lr, 0.5f, 5, 180, 5);
        mMusicProc.run();
        
        
        mObjPool  = new DemoObjPool();
        Game.get().getEventManager().addListener(ICameraMoveEvent.EVENT_TYPE, this);
        Game.get().getEventManager().addListener(IDemoObjCatchedEvent.EVENT_TYPE, this);
        
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
        if(event.getType().equals(IDemoObjCatchedEvent.EVENT_TYPE)) {
            // show an explosion when object was catched
            final IDemoObjCatchedEvent e = (IDemoObjCatchedEvent) event;
            final GameObj obj = e.getGameObj();
            final float[] pos = obj.getPosition();
            
            FireAndForgetEffectProc p = mExplosions.obtain();
            p.getGameObj().setPosition(pos[0], pos[1], pos[2]);
            p.run();
            
            // update scoreboard
            ++mScore;
            //mScoreText.setText("Score: " + ++mScore);
            
            // start music again if score reaches multiple of 20
            if (mScore % 40 == 0) {
                mMusicProc.run();
            }
        }  
        
        if(event.getType().equals(ICameraMoveEvent.EVENT_TYPE)) {
            // get position of the camera
            final ICameraMoveEvent e = (ICameraMoveEvent) event;
            final float[] camPos = e.getCamPosition();
            mZPos = camPos[2];
            
            // remove all objects in monkey layer behind the camera
            // TODO prioB: not recommended to add/remove game obj in
            // event handler because this might add new event listeners
            for (int i = Game.get().getGameObjCnt()-1; i >= 0; i--) {
                final GameObj obj = Game.get().getObjByListIdx(i);
                if (!mMonkeyLayer.covers(obj.getLayer())) {
                    continue;
                }
                final float[] pos = obj.getPosition();
                if (pos[2] > camPos[2]) {
                    Game.get().removeGameObj(obj);
                    obj.recycle();
                }
            }
            return false;
        }
        
        if(event.getType().equals(IDemoObjCatchedEvent.EVENT_TYPE)) {
            final IDemoObjCatchedEvent e = (IDemoObjCatchedEvent) event;
            final GameObj obj = e.getGameObj();
            Game.get().removeGameObj(obj);
            obj.recycle();
        } 
        
        return false;
    }
    
    public static class ExplosionPool extends Pool<FireAndForgetEffectProc> {
        
        private int mNumInst;
        
        @Override
        protected FireAndForgetEffectProc allocatePoolItem() {
            // setup explosions as pooled items
            GameObj obj = new GameObj("explosion_" + mNumInst++, mOthersLayer);
            
            final ETC1TextureResource texture = new ETC1TextureResource(Asset.GRADIENT_RED__PKM);
            final Material mat = CircularShapeEmitter.makeMaterial(texture, 
                    TextureObjOptions.LINEAR_CLAMP);
            obj.addComponent(mat);
            final CircularShapeEmitter c = new CircularShapeEmitter(100, true, 
                    ClockType.REALTIME);
            c.setSpeed(1);
            c.setRadius(2);
            c.setSpread(1);
            
            obj.addComponent(c);
            final SceneDrawable d = new SceneDrawable(RenderPass.ALPHA);
            obj.addComponent(d);
            
            obj.rotate(1, 0, 0, 90);
            return new FireAndForgetEffectProc(obj);
        }      
    }
    
}
