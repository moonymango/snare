package com.moonymango.snareDemo.playground;

import com.moonymango.snare.res.texture.ETC1TextureResource;
import com.moonymango.snare.ui.scene3D.mesh.SquareMesh;
import com.moonymango.snare.ui.scene3D.rendering.PlainTextureEffect;
import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.SimpleGameState;
import com.moonymango.snare.game.logic.OrbitPositionModifier;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.GradientGenerator;
import com.moonymango.snare.res.texture.GradientGenerator.ColorSamples;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.rendering.BlastEffect;
import com.moonymango.snare.ui.scene3D.rendering.LightningBolt;
import com.moonymango.snare.ui.scene3D.rendering.LightningBolt.LinearBoltPointGenerator;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;
import com.moonymango.snare.ui.scene3D.rendering.WeaponParticleEmitter;
import com.moonymango.snare.util.EasingProfile;

class GameState extends SimpleGameState implements IEventListener {

    private final float[] mLookAt = {0, 0, 0, 1};
    private final float[] mUp = {0, 1, 0, 0};
    
    private PerspectiveCamera mCam;
    private GameObj mObj;
    private GameObj mObj2;
    private GameObj mObj3;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        // camera object moves around the scene, therefore adjust
        // orientation to keep particle systems in field-of-view
        if (mCam != null) {
            mCam.lookAt(mLookAt[0], mLookAt[1], mLookAt[2], mUp[0], mUp[1], 
                    mUp[2]);
        }
        
        
        SceneDrawable d = (SceneDrawable) mObj.getComponent(ComponentType.RENDERING);
        if (d.isFinished())
            d.reset();
        
        d = (SceneDrawable) mObj2.getComponent(ComponentType.RENDERING);
        if (d.isFinished())
            d.reset();
        d = (SceneDrawable) mObj3.getComponent(ComponentType.RENDERING);
        if (d.isFinished())
            d.reset();
        
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        final Scene3D s = new Scene3D();
        
        mCam = new PerspectiveCamera();
        GameObj obj = new GameObj("camera");
        obj.addComponent(mCam);
        obj.setPosition(0, 1, 4);
        Game.get().addGameObj(obj);
        
        mCam.lookAt(0, 0, 0, 0, 1, 0);
        mCam.setNearFarPlane(1, 30);
        mCam.setFieldOfView(60);
        s.setCamera(mCam);
        
        // let camera revolve around the scene
        OrbitPositionModifier mod = new OrbitPositionModifier(obj, mLookAt, mUp, 10);
        mod.run();
                    
        Game.get().getPrimaryView().pushScreenElement(s);
        
        obj = new GameObj("grid");
        obj.addComponent(new LinesEffect());
        obj.addComponent(new GridMesh(5, 5));
        obj.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
         
        mObj = new GameObj("particle");
        mObj.addComponent(new WeaponParticleEmitter(20, 0.03f, 1, 4));
        mObj.addComponent(new SceneDrawable(RenderPass.DYNAMIC));
        mObj.setPosition(-2, 0, 1);
        Game.get().addGameObj(mObj);
        
        obj = new GameObj("beam");
        mat = LightningBolt.makeMaterial(new BitmapTextureResource(Asset.BEAM_TEX),
                TextureObjOptions.LINEAR_REPEAT);
        obj.addComponent(mat);

        LightningBolt lb = new LightningBolt(new LinearBoltPointGenerator(8), 25);
        lb.setVisibleSegments(0, 7); //.setIntensity(0.05f);
        obj.addComponent(lb);
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA));
        obj.setPosition(-2, 0, 0).setScale(0.5f, 1, 1);
        Game.get().addGameObj(obj);
        mObj = obj;
        
        obj = new GameObj("lightning");
        mat = LightningBolt.makeMaterial(new BitmapTextureResource(Asset.BEAM_TEX),
                TextureObjOptions.LINEAR_REPEAT);
        obj.addComponent(mat);
        lb = new LightningBolt(new LinearBoltPointGenerator(8));
        lb.setIntensity(0.05f).setSpeed(33).setThickness(0.08f);
        obj.addComponent(lb);
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA));
        obj.setPosition(-2, 0, -1).setScale(0.5f, 1, 1);
        Game.get().addGameObj(obj);
        
        final ColorSamples samples = new ColorSamples(2, 1);
        samples.setColumn(0, 1, 1, 1, 1);
        samples.setColumn(1, 0, 0, 1, 1);
        GradientGenerator gg = new GradientGenerator(samples);
        
        // choose random name, so that we do not get the same texture from
        // the resource cache when reloading
        BitmapTextureResource bm = new BitmapTextureResource("gradient" 
                + Game.get().getRandomString(), TextureSize.S_4, gg, gg, gg);
        
        mObj2 = new GameObj("blast0");
        mat = BlastEffect.makeMaterial(bm, TextureObjOptions.NEAREST_CLAMP);
        mObj2.addComponent(mat);
        BlastEffect be = new BlastEffect(16, EasingProfile.SQUARE_OUT, 
                EasingProfile.SQUARE_IN, 2, ClockType.VIRTUAL); 
        mObj2.addComponent(be);
        mObj2.addComponent(new SceneDrawable(RenderPass.ALPHA));
        mObj2.setPosition(1, 0, -2).rotate(1, 0, 0, 90);
        Game.get().addGameObj(mObj2);
        
        mObj3 = new GameObj("blast1");
        mat = BlastEffect.makeMaterial(bm, TextureObjOptions.LINEAR_REPEAT);
        mObj3.addComponent(mat);
        be = new BlastEffect(16, EasingProfile.SQUARE_OUT, 
                EasingProfile.LINEAR, 2, ClockType.VIRTUAL); 
        mObj3.addComponent(be);
        mObj3.addComponent(new SceneDrawable(RenderPass.ALPHA));
        mObj3.setPosition(-1, 0, -2).rotate(1, 0, 0, 90);
        Game.get().addGameObj(mObj3);


        // add square with logo texture
        ETC1TextureResource etc = new ETC1TextureResource(Asset.NO_PARKING__PKM);
        mat = PlainTextureEffect.makeMaterial(etc, TextureObjOptions.LINEAR_CLAMP);
        obj = new GameObj("square");
        obj.addComponent(mat);
        obj.addComponent(new SquareMesh());
        obj.addComponent(new PlainTextureEffect());
        obj.addComponent(new SceneDrawable(RenderPass.ALPHA));
        Game.get().addGameObj(obj);

        Game.get().getEventManager().addListener(ITouchEvent.EVENT_TYPE, 
                this);
    }

    @Override
    public boolean handleEvent(IEvent event) {
        
        final SceneDrawable d = (SceneDrawable) mObj.getComponent(ComponentType.RENDERING);
        d.reset();
        return false;
    }
    
}
