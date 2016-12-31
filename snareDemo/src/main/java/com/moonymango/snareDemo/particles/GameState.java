package com.moonymango.snareDemo.particles;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.game.IGameStateLogic;
import com.moonymango.snare.game.logic.OrbitPositionModifier;
import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource.Channel;
import com.moonymango.snare.res.texture.ConstGenerator;
import com.moonymango.snare.res.texture.GradientGenerator;
import com.moonymango.snare.res.texture.GradientGenerator.ColorSamples;
import com.moonymango.snare.res.texture.ShapeGenerator;
import com.moonymango.snare.res.texture.ShapeGenerator.Shape;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.mesh.GridMesh;
import com.moonymango.snare.ui.scene3D.rendering.LinesEffect;
import com.moonymango.snare.ui.scene3D.rendering.PyramidShapeEmitter;
import com.moonymango.snare.ui.scene3D.rendering.SceneDrawable;


class GameState implements IGameState, IGameStateLogic {
    
    private final float[] mLookAt = {0, 1, 0, 1};
    private final float[] mUp = {0, 1, 0, 0};
    private PerspectiveCamera mCam;
    
    private PyramidShapeEmitter mE1;
    private PyramidShapeEmitter mE2;
    
    private float mColorY;
    private float mDelta = 0.001f;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        // change particle effect texture fetch coords to change particle
        // colors
        mDelta = mColorY > 1 ? -0.001f : mDelta;
        mDelta = mColorY < 0 ? +0.001f : mDelta;
        mColorY += mDelta;
        mE1.setColorY(mColorY);
        mE2.setColorY(1-mColorY);
        
        // camera object moves around the scene, therefore adjust
        // orientation to keep particle systems in field-of-view
        if (mCam != null) {
            mCam.lookAt(mLookAt[0], mLookAt[1], mLookAt[2], mUp[0], mUp[1], 
                    mUp[2]);
        }
        return null;
    }

    @Override
    public void onActivate(IGameState previous) {
        
        mCam = new PerspectiveCamera();
        GameObj obj = new GameObj("camera");
        obj.addComponent(mCam);
        obj.setPosition(9, 5, 9);
        Game.get().addGameObj(obj);
        
        mCam.lookAt(0, 1, 0, 0, 1, 0);
        mCam.setFieldOfView(30);
        Scene3D s = new Scene3D();
        s.setCamera(mCam);
        Game.get().getPrimaryView().pushScreenElement(s);
        
        // let camera revolve around the scene
        OrbitPositionModifier mod = new OrbitPositionModifier(obj, mLookAt, mUp, 10);
        mod.run();
       
        obj = new GameObj("grid");
        obj.addComponent( new LinesEffect());
        obj.addComponent(new GridMesh(5, 5));
        obj.addComponent(new SceneDrawable(RenderPass.ENVIRONMENT));
        Material mat = new Material();
        mat.setColor(Material.LINE_COLOR_IDX, 0, 0, 1, 1);
        obj.addComponent(mat);
        Game.get().addGameObj(obj);
        
        // generate particle effect color texture:
        final ColorSamples rgb = new ColorSamples(2, 2);
        rgb.set(0, 0, 1, 1, 1, 1)
            .set(1, 0, 1, 0, 0, 0)
            .set(0, 1, 0, 0, 1, 1)
            .set(1, 1, 1, 1, 1, 0);
        
        GradientGenerator gg = new GradientGenerator(rgb);
        
        BitmapTextureResource colorTex = new BitmapTextureResource("color", 
                TextureSize.S_32, gg, gg, gg, gg);
        // generate particle shape texture
        ShapeGenerator sg = new ShapeGenerator(Shape.CIRCLE, 0.3f, false);
        ConstGenerator cg = new ConstGenerator(1);
        BitmapTextureResource shapeTex = new BitmapTextureResource("shape", 
                TextureSize.S_32, null, null, cg, sg);
        
        // game objects
        GameObj obj1 = new GameObj("emitter1");
        mat = PyramidShapeEmitter.makeMaterial(colorTex, shapeTex);
        obj1.addComponent(mat);
        mE1 = new PyramidShapeEmitter(200, Channel.A, ClockType.VIRTUAL);
        mE1.setHeight(2);
        mE1.setSpread(2);
        mE1.setShape(2f);
        obj1.addComponent(mE1);
        obj1.addComponent(new SceneDrawable(RenderPass.ALPHA));
        obj1.setPosition(1.5f, 0, 1.5f);
        //obj.setScale(0.2f, 1, 1);
        
        GameObj obj2 = new GameObj("emitter2");
        mat = PyramidShapeEmitter.makeMaterial(colorTex, shapeTex);
        obj2.addComponent(mat);
        mE2 = new PyramidShapeEmitter(200, Channel.A, ClockType.VIRTUAL);
        mE2.setHeight(2);
        mE2.setSpeed(3);
        mE2.setSpread(0.2f);
        mE2.setShape(-0.2f);
        
        obj2.addComponent(mE2);
        obj2.addComponent(new SceneDrawable(RenderPass.ALPHA));
        obj2.setPosition(-1.5f, 0, -1.5f);
               
        Game.get().addGameObj(obj1);
        Game.get().addGameObj(obj2);
        
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
}

