package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.ui.scene3D.BaseSceneDrawable;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.ui.scene3D.Scene3D.DrawBundle;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.MatrixStack;

/**
 * Drawable that allows to adjust its position relative to
 * game object position. It also allows to set rotation
 * and scale independently from the game object.
 * TODO prioD: this is just a workaround missing child game objects
 */
public class OffsetSceneDrawable extends BaseSceneDrawable {

    protected final float[] mTransform = new float[16];
    private final float[] mOffset = new float[4];
    private final float[] mRotation = {0, 1, 0, 0};
    private final float[] mScale = {1, 1, 1, 0};
    private final float[] mRotScaleMatrix = new float[16];
    
    private final float[] mTmpVec = new float[4];
    private final float[] mTmpMat = new float[16];
    
    public OffsetSceneDrawable(IGame game, RenderPass rp)
    {
        super(game, rp);
        MatrixAF.setIdentityM(mRotScaleMatrix, 0);
    }
    
    public void setRotation(float x, float y, float z, float angle) {
        mRotation[0] = x;
        mRotation[1] = y;
        mRotation[2] = z;
        mRotation[3] = angle;
        updateMatrix();
    }
    
    public void setScale(float x, float y, float z) {
        mScale[0] = x;
        mScale[1] = y;
        mScale[2] = z;
        updateMatrix();
    }
    
    private void updateMatrix() {
        // get new transform
        MatrixAF.setIdentityM(mRotScaleMatrix, 0);  
        MatrixAF.rotateM(mRotScaleMatrix, 0, mRotation[3], mRotation[0], mRotation[1], mRotation[2]);
        MatrixAF.scaleM(mRotScaleMatrix, 0, mScale[0], mScale[1], mScale[2]);
    }

    public void draw(Scene3D scene, DrawBundle bundle, RenderPass pass) {
        
        final float[] pos = mGameObj.getPosition();
        for (int i = 0; i < 3; i++) {
            mTmpVec[i] = pos[i] + mOffset[i];
        }
        MatrixAF.setIdentityM(mTmpMat, 0);
        MatrixAF.translateM(mTmpMat, 0, mTmpVec[0], mTmpVec[1], mTmpVec[2]);
        MatrixAF.multiplyMM(mTransform, 0, mTmpMat, 0, mRotScaleMatrix, 0);
        
        final MatrixStack ms = scene.getViewTransformStack();
        ms.pushMatrix(mTransform);
        
        final int o = bundle.getOrdinal();
        if (o == 0) {
            // init value because this is the first draw call to this drawable
            // in actual scene
            mIsFinished = true;
        }
        mIsFinished &= !mEffects[o].render(scene, mMesh, mMat, mGameObj, pass);
        
        ms.popMatrix();
    } 
    
    /**
     * Sets position relative to game object postion.
     * @param x
     * @param y
     * @param z
     */
    public void setOffet(float x, float y, float z) {
        mOffset[0] = x;
        mOffset[1] = y;
        mOffset[2] = z;
    }

}
