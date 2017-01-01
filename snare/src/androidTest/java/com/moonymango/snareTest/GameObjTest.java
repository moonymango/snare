package com.moonymango.snareTest;


import android.opengl.Matrix;
import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.GameObjLayer;
import com.moonymango.snare.util.Geometry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class GameObjTest  {

       
    private final static float POS_X = 5.2f;
    private final static float POS_Y = 20.6f;
    private final static float POS_Z = 8.9f;
    private final static float SCALE_X = 1.5f;
    private final static float SCALE_Y = 0.8f;
    private final static float SCALE_Z = 1.1f;
    private final static float ROT_X = 1.0f;
    private final static float ROT_Y = 0f;
    private final static float ROT_Z = 0f;
    private final static float ROT_ANGLE = 90;
    
    private float[] mRefTransform = new float[16];
    private float[] mRefTransformInv = new float[16];
    
    @Before
    public void setUp() throws Exception {
        
        Matrix.setIdentityM(mRefTransform, 0);
        Matrix.translateM(mRefTransform, 0, POS_X, POS_Y, POS_Z);
        Matrix.rotateM(mRefTransform, 0, ROT_ANGLE, ROT_X, ROT_Y, ROT_Z);
        Matrix.scaleM(mRefTransform, 0, SCALE_X, SCALE_Y, SCALE_Z);
        
        Matrix.invertM(mRefTransformInv, 0, mRefTransform, 0);
    }

    @Test
    public void testTransform() {
        
        GameObj obj = new GameObj("my object");
        obj.setPosition(POS_X, POS_Y, POS_Z);
        obj.rotate(ROT_X, ROT_Y, ROT_Z, ROT_ANGLE);
        obj.setScale(SCALE_X, SCALE_Y, SCALE_Z);
        
        obj.onUpdateTransform(0, 0, 0);
        
        final float[] transform = obj.getToWorld();
        final float[] transformInv = obj.getFromWorld();
        
        for (int i = 0; i < 16; i++) {
            assertEquals(mRefTransform[i], transform[i], Geometry.PRECISION);
            assertEquals(mRefTransformInv[i], transformInv[i], Geometry.PRECISION);
        }
       
    }

    @Test
    public void testVectors() {
        GameObj obj = new GameObj("my object");
        
        // forward points to z
        float[] vec = obj.getForwardVector();
        assertEquals(1, vec[2], Geometry.PRECISION);
        
        // rotate 90° about y, forward should point towards x
        obj.rotate(0, 1, 0, 90);
        vec = obj.getForwardVector();
        assertEquals(1, vec[0], Geometry.PRECISION);
        
        // rotate again 90° about x, up should point towards z
        obj.rotate(1, 0, 0, 90);
        vec = obj.getUpVector();
        assertEquals(1, vec[2], Geometry.PRECISION);
        
    }

    @Test
    public void testGameObjLayer() {
        final GameObjLayer l0 = new GameObjLayer("l0", 0x01);
        final GameObjLayer l1 = new GameObjLayer("l1", 0x10);
        final GameObjLayer l2 = l0.or(l1);
        
        assertEquals(l0.covers(l1), false);
        assertEquals(l0.covers(l2), true);
    }
}
