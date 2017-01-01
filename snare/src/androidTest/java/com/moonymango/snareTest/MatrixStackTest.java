package com.moonymango.snareTest;

import android.opengl.Matrix;
import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.MatrixStack;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class MatrixStackTest {

    private MatrixStack mStack;
    private float[] mTranslate = new float[16];
    private float[] mScale = new float[16];
    private float[] mRotate = new float[16];
    
    public MatrixStackTest() {
        Matrix.setIdentityM(mRotate, 0);
        Matrix.setIdentityM(mScale, 0);
        Matrix.setIdentityM(mTranslate, 0);
        Matrix.rotateM(mRotate, 0, 90, 1, 1, 1);
        Matrix.translateM(mTranslate, 0, 10, 10, 10);
        Matrix.scaleM(mScale, 0, 1.5f, 1.5f, 1.5f);
    }

    @Before
    public void setUp() {
        mStack = new MatrixStack(10);
    }

    @Test
    public void testPushPop() {
        float[] result = mStack.getProduct();
        assertTrue(result == null);
        assertTrue(mStack.isEmpty());
        assertTrue(mStack.getTop() == null);
        
        mStack.pushMatrix(mRotate);
        assertTrue(Arrays.equals(mStack.getTop(), mRotate));

        mStack.pushMatrix(mTranslate);
        assertTrue(Arrays.equals(mStack.getTop(), mTranslate));
        
        mStack.pushMatrix(mScale);
        assertTrue(Arrays.equals(mStack.getTop(), mScale));
        
        result = mStack.getProduct();
        assertTrue(result != null);
        assertTrue(!mStack.isEmpty());
        
        mStack.popMatrix();
        assertTrue(Arrays.equals(mStack.getTop(), mTranslate));
        
        mStack.popMatrix();
        assertTrue(Arrays.equals(mStack.getTop(), mRotate));
        
        mStack.popMatrix();
        assertTrue(mStack.getTop() == null);
        assertTrue(mStack.isEmpty());
        result = mStack.getProduct();
        assertTrue(result == null);
        
        mStack.pushMatrix(mRotate);
        mStack.pushMatrix(mTranslate);
        mStack.pushMatrix(mScale);
        result = mStack.getProduct();
        assertTrue(result != null);
        assertTrue(!mStack.isEmpty());
        
        mStack.reset();
        assertTrue(mStack.isEmpty());
        result = mStack.getProduct();
        assertTrue(result == null);
        
    }

    @Test
    public void testValues() {
        mStack.pushMatrix(mRotate);
        mStack.pushMatrix(mScale);
        mStack.pushMatrix(mTranslate);
        
        float[] result;
        
        float[] reference = new float[16];
        Matrix.setIdentityM(reference, 0);
        Matrix.rotateM(reference, 0, 90, 1, 1, 1);
        Matrix.scaleM(reference, 0, 1.5f, 1.5f, 1.5f);
        Matrix.translateM(reference, 0, 10, 10, 10);
        
        float[] referenceInv = new float[16];
        Matrix.invertM(referenceInv, 0, reference, 0);
        
        float[] referenceNormal = new float[16];
        Matrix.transposeM(referenceNormal, 0, referenceInv, 0);
        
        result = mStack.getProduct();
        //assertTrue(Arrays.equals(reference, result));
        assertArrayEquals(reference, result, Geometry.PRECISION);
        
        result = new float[16];
        mStack.copyProduct(result);
        assertArrayEquals(reference, result, Geometry.PRECISION);
        
        result = mStack.getInv();
        assertArrayEquals(referenceInv, result, Geometry.PRECISION);

        result = new float[16];
        mStack.copyInv(result);
        assertArrayEquals(referenceInv, result, Geometry.PRECISION);
        
        result = mStack.getInvTranspose();
        assertArrayEquals(referenceNormal, result, Geometry.PRECISION);
        
        result = new float[16];
        mStack.copyInvTranspose(result);
        assertArrayEquals(referenceNormal, result, Geometry.PRECISION);
    }

    @Test
    public void testMatrixNormalize() {
        // actually this does'nt test the stack, but it is also
        // related to matrix stuff
        float[] mat = new float[16];
        float[] reference = new float[16];
        MatrixAF.setIdentityM(reference, 0);
        MatrixAF.setIdentityM(mat, 0);
        MatrixAF.rotateM(reference, 0, 26.3f, 0, 0, -1);
        MatrixAF.rotateM(mat, 0, 26.3f, 0, 0, -1);
        
        MatrixAF.orthoNormalize(mat);
        for (int i = 0; i < 16; i++) {
            assertEquals(reference[i], mat[i], Geometry.PRECISION);
        }
    }
}
