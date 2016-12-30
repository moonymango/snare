package stachelsau.snare.ui.scene3D.rendering;

import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj.ComponentType;
import stachelsau.snare.physics.BaseBoundingVolume;
import stachelsau.snare.ui.scene3D.BaseSceneDrawable;
import stachelsau.snare.ui.scene3D.RenderPass;
import stachelsau.snare.ui.scene3D.Scene3D;
import stachelsau.snare.ui.scene3D.Scene3D.DrawBundle;
import stachelsau.snare.ui.scene3D.Scene3DOptions;
import stachelsau.snare.util.MatrixStack;


public class SceneDrawable extends BaseSceneDrawable {
    
    public SceneDrawable(RenderPass rp) {
        super(rp);
    }

    public void draw(Scene3D scene, DrawBundle bundle) 
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
        mIsFinished &= !mEffects[o].render(scene, mMesh, mMat, mGameObj);
        
        ms.popMatrix();
    }

}
