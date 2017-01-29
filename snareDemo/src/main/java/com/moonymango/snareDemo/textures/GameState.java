package com.moonymango.snareDemo.textures;

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
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.game.logic.OrbitPositionModifier;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.data.ImportTransformBlender;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Light.LightType;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.mesh.Mesh;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.PlainTextureEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;

class GameState implements IGameState, IGameStateLogic,
        IEventListener {

    private PerspectiveCamera mCam;
    private GameObj mEarth;
    private GameObj mMoon;
     
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
    	// rotate game object
        mEarth.rotate(0, 1, 0, 1);
        
        mCam.lookAt(0, 0, 0, 0, 1, 0);
        
        // return null means we stay in this game state
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
    	
        GameSettings s = Game.get().getSettings();
        s.RENDER_OPTIONS.BG_COLOR_B = 0; //1;
        s.RENDER_OPTIONS.BG_COLOR_R = 0; //0.5f;
        s.RENDER_OPTIONS.BG_COLOR_G = 0; //0.5f;
        s.PRINT_STATS = true;
        
        // input events
        s.INPUT_EVENT_MASK.SCROLL_ENABLED = true;
        s.INPUT_EVENT_MASK.FLING_ENABLED = true;
        s.INPUT_EVENT_MASK.SCALE_ENABLED = true;
        s.INPUT_EVENT_MASK.DOUBLE_TAP_ENABLED = true;
        
    	// startup: create the scene and objects
        final Scene3D sc = new Scene3D();
        Game.get().getPrimaryView().pushScreenElement(sc);
        
        // +++ camera +++
        mCam = new PerspectiveCamera();
        GameObj camObj = new GameObj("camera");
        camObj.addComponent(mCam);
        camObj.setPosition(0, 3, 10);
        Game.get().addGameObj(camObj);
        
        mCam.lookAt(0, 0, 0, 0, 1, 0);
        mCam.setNearFarPlane(2, 30);
        mCam.setFieldOfView(30);
        sc.setCamera(mCam);
        
        // ++++ lights ++++
        GameObj lightObj = new GameObj("directionalLight");
        Light light = new Light(LightType.DIRECTIONAL);
        light.setColor(1, 1, 1, 0);
        lightObj.addComponent(light);
        lightObj.rotate(0, 1, 0, 90);
        Game.get().addGameObj(lightObj);
        
        lightObj = new GameObj("ambientLight");
        light = new Light(LightType.AMBIENT);
        light.setColor(1, 1, 1, 0);
        lightObj.addComponent(light);
        Game.get().addGameObj(lightObj);
                    
        
        GameObj grid = new GameObj("grid");
        grid.addComponent( new LinesEffect());
        grid.addComponent(new GridMesh(20, 20));
        grid.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 1, 1, 1, 1);
        grid.setPosition(0, -1, 0);
        grid.addComponent(mat);
        Game.get().addGameObj(grid);
        
        // ++++ cube ++++
        // load shape from asset file
        //MeshResource meshRes = new MeshResource(Asset.CUBE3DS_UV_MESH, 
          //      new ImportTransformCenterToOrigin(true));
        MeshResource meshRes = new MeshResource(Asset.SPHERE_UV_MESH, 
                new ImportTransformBlender());
        
        BitmapTextureResource texMoon = new BitmapTextureResource(Asset.MOON_TEX);
        BitmapTextureResource texEarth = new BitmapTextureResource(Asset.EARTH_TEX);
        BitmapTextureResource texSpace = new BitmapTextureResource(Asset.SPACE2_TEX);
        
        
        mEarth = new GameObj("earth"); 
        
        mEarth.addComponent(new PlainTextureEffect());
        mEarth.addComponent(new Mesh(meshRes));
        mEarth.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        mat = PlainTextureEffect.makeMaterial(texEarth, TextureObjOptions.LINEAR_REPEAT);
        mEarth.addComponent(mat);
        //mEarth.setScale(0.75f, 0.75f, 0.75f);		// set size 
        mEarth.setPosition(0, 0, 0);				// put obj to origin
        
        Game.get().addGameObj(mEarth);
        
        mMoon = new GameObj("moon"); 
        
        mMoon.addComponent(new PlainTextureEffect());
        mMoon.addComponent(new Mesh(meshRes));
        mMoon.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        mat = PlainTextureEffect.makeMaterial(texMoon, TextureObjOptions.LINEAR_REPEAT);
        mMoon.addComponent(mat);
        mMoon.setScale(0.75f, 0.75f, 0.75f);       // set size 
        mMoon.setPosition(-4, 0, 0);              
        
        Game.get().addGameObj(mMoon);
        
        // space background
        GameObj space = new GameObj("space"); 
        
        space.addComponent(new PlainTextureEffect());
        space.addComponent(new Mesh(meshRes));
        space.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        mat = new Material();
        mat.setColor(Material.DIFFUSE_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.AMBIENT_COLOR_IDX, 1, 1, 1, 1);
        mat.setColor(Material.OUTLINE_COLOR_IDX, 0, 0, 0, 1);
        mat.addTextureUnit(new TextureUnit(0, texSpace, TextureObjOptions.LINEAR_REPEAT));
        space.addComponent(mat);
        space.setScale(10f, 10, 10f);       // set size 
        space.setPosition(0, 0, 0);              
        
        Game.get().addGameObj(space);
        
        // make objects move
        float[] axis= {0, 1, 0, 0};
        float[] point = {0, 10, 0, 0};
        OrbitPositionModifier om = new OrbitPositionModifier(mMoon, axis, 15);
        om.run();
        
        om = new OrbitPositionModifier(camObj, point, axis, 5);
        om.run();
        
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
            //handleScroll((IScrollEvent) event);
        }
        if (event.getType() == IFlingEvent.EVENT_TYPE) {
            //handleFling((IFlingEvent) event);
        }
        if (event.getType() == IScaleEvent.EVENT_TYPE) {
            //handleScale((IScaleEvent) event);
        }
        
        if (event.getType() == ITouchEvent.EVENT_TYPE) {
            final ITouchEvent e = (ITouchEvent) event;
            if (e.getTouchAction() == TouchAction.DOUBLE_TAB) {
                //handleDoubleTap();
            }
        }
        return false;
    }
    
   

    
}
