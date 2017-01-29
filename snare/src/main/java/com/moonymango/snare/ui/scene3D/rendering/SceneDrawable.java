package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.physics.BaseBoundingVolume;
import com.moonymango.snare.ui.scene3D.BaseSceneDrawable;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.Scene3D.DrawBundle;
import com.moonymango.snare.ui.scene3D.Scene3DOptions;
import com.moonymango.snare.util.MatrixStack;


public class SceneDrawable extends BaseSceneDrawable {
    
    public SceneDrawable(RenderPass rp) {
        super(rp);
    }

    public void draw(Scene3D scene, DrawBundle bundle, RenderPass pass)
    {
        if (isFinished())
            return;
        
        // frustrum culling
        final Scene3DOptions ro = Game.get().getSettings().SCENE_OPTIONS;
        final BaseBoundingVolume bv = (BaseBoundingVolume) 
                mGameObj.getComponent(ComponentType.BOUNDING_VOLUME);
        if (bv != null && ro.ENABLE_FRUSTRUM_CULLING 
                && !bv.isInFrustrum(scene.getCamera())) {
            return;
        }
               
        // use the game objects transform
        final MatrixStack ms = scene.getViewTransformStack();
        ms.pushMatrix(mGameObj.getToWorld());
        
        final int o = bundle.getOrdinal();
        if (o == 0) {
            // init value because this is the first draw call to this drawable
            // in actual scene
            mIsFinished = true;
        }
        mIsFinished &= !mEffects[o].render(scene, mMesh, mMat, mGameObj, pass);
        
        ms.popMatrix();
    }

}
