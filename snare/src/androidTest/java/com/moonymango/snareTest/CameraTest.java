package com.moonymango.snareTest;


import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.VectorAF;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class CameraTest {
    
    private DebugPerspectiveCamera mCamera;
    private GameObj mObj;
        
    @Before
    public void setUp() throws Exception {
        mCamera = new DebugPerspectiveCamera();
        mObj = new GameObj("camera");
        mObj.addComponent(mCamera);
    }

    @Test
    public void testLookAt() {
        mObj.setPosition(1, 0, 0);
        mCamera.lookAt(0, 0, 0, 0, 1, 0);
        float[] fw = mObj.getForwardVector();
        assertEquals(-1, fw[0], Geometry.PRECISION);
        assertEquals(0, fw[1], Geometry.PRECISION);
        assertEquals(0, fw[2], Geometry.PRECISION);
        float[] left = mObj.getLeftVector();
        assertEquals(0, left[0], Geometry.PRECISION);
        assertEquals(0, left[1], Geometry.PRECISION);
        assertEquals(1, left[2], Geometry.PRECISION);
        float[] up = mObj.getUpVector();
        assertEquals(0, up[0], Geometry.PRECISION);
        assertEquals(1, up[1], Geometry.PRECISION);
        assertEquals(0, up[2], Geometry.PRECISION);
        
        mObj.setPosition(0, 1, 0);
        mCamera.lookAt(0, 0, 0, 0, 0, -1);
        fw = mObj.getForwardVector();
        assertEquals(0, fw[0], Geometry.PRECISION);
        assertEquals(-1, fw[1], Geometry.PRECISION);
        assertEquals(0, fw[2], Geometry.PRECISION);
        left = mObj.getLeftVector();
        assertEquals(-1, left[0], Geometry.PRECISION);
        assertEquals(0, left[1], Geometry.PRECISION);
        assertEquals(0, left[2], Geometry.PRECISION);
        up = mObj.getUpVector();
        assertEquals(0, up[0], Geometry.PRECISION);
        assertEquals(0, up[1], Geometry.PRECISION);
        assertEquals(-1, up[2], Geometry.PRECISION);
    }

    @Test
    public void testFrustrumPlaneExtraction() {
        
        // adjust position, FOV and looking direction so that right frustrum plane is {1, 0, 0, 1}
        // left plane is {0, 0, 1, 0}
        mCamera.setFieldOfView(90);
        mObj.setPosition(1, 0, 0);
        mCamera.lookAt(0, 0, -1, 0, 1, 0);
        mObj.onUpdateTransform(1, 1, 1);
        mCamera.onPreDraw();
        
        // test pointX should have distance of -1 to right plane 
        final float[] testPointX = {2, 0, 0, 1};
        final float distX = mCamera.getPlaneDistance(testPointX, PerspectiveCamera.FRUSTRUM_RIGHT_PLANE_IDX);
        
        // test pointZ should have distance of -1 to left plane
        final float[] testPointZ = {0, 0, 1, 1};
        final float distZ = mCamera.getPlaneDistance(testPointZ, PerspectiveCamera.FRUSTRUM_LEFT_PLANE_IDX);
        
        assertEquals(-1.0f, distX, 0.001);
        assertEquals(-1.0f, distZ, 0.001);        
    }

    @Test
    public void testRayDirection() {
        mCamera.setFieldOfView(90);
        mObj.setPosition(0, 0, -1);
        mCamera.lookAt(1, 0, 0, 0, 1, 0);
        mCamera.setScreenDimensions(100, 100);
        mObj.onUpdateTransform(1, 1, 1);
        mCamera.onPreDraw();
        
        // direction screen center should be (1, 0, 1)
        float[] direction = mCamera.getRayDirection(50, 50);
        float[] ref = new float[4];
        ref[0] = 1;
        ref[1] = 0;
        ref[2] = 1;
        ref[3] = 0;
        VectorAF.normalize(ref);
        float dot = VectorAF.dot(direction, ref);
        assertEquals(1, dot, 0.001);
        // this should also be the viewing direction
        final float[] vd = mCamera.getViewDirection();
        assertEquals(true, VectorAF.compare(vd, ref, Geometry.PRECISION));
        
        
        // left of screen points to (1, 0, 0)
        direction = mCamera.getRayDirection(0, 50);
        ref[0] = 1;
        ref[1] = 0;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        dot = VectorAF.dot(direction, ref);
        assertEquals(1, dot, 0.001);
        
        // right of screen points to (0, 0, 1)
        direction = mCamera.getRayDirection(100, 50);
        ref[0] = 0;
        ref[1] = 0;
        ref[2] = 1;
        ref[3] = 0;
        VectorAF.normalize(ref);
        dot = VectorAF.dot(direction, ref);
        assertEquals(1, dot, 0.001);
        
        // mid top of screen points to (0.7, -1, 0.7)
        // (near plane distance is 1)
        direction = mCamera.getRayDirection(50, 0);
        ref[0] = 1;
        ref[1] = 0;
        ref[2] = 1;
        ref[3] = 0;
        VectorAF.normalize(ref);
        ref[1] = -1;
        VectorAF.normalize(ref);
        dot = VectorAF.dot(direction, ref);
        assertEquals(1, dot, 0.001);
        
        // mid bottom of screen points to (0.7, 1, 0.7)
        // (near plane distance is 1)
        direction = mCamera.getRayDirection(50, 100);
        ref[0] = 1;
        ref[1] = 0;
        ref[2] = 1;
        ref[3] = 0;
        VectorAF.normalize(ref);
        ref[1] = 1;
        VectorAF.normalize(ref);
        dot = VectorAF.dot(direction, ref);
        assertEquals(1, dot, 0.001);
        
    }

    @Test
    public void testRayDirectionII() {
        // same test as above but using different axis
        mCamera.setFieldOfView(90);
        mObj.setPosition(-1, 0, 0);
        mCamera.lookAt(0, 1, 0, 0, 0, 1);
        mCamera.setScreenDimensions(100, 100);
        mObj.onUpdateTransform(1, 1, 1);
        mCamera.onPreDraw();
        
        // direction for screen center should be (1, 1, 0)
        float[] direction = mCamera.getRayDirection(50, 50);
        float[] ref = new float[4];
        ref[0] = 1;
        ref[1] = 1;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        assertEquals(true, VectorAF.compare(direction, ref, Geometry.PRECISION));
        
        // left of screen points to (0, 1, 0)
        direction = mCamera.getRayDirection(0, 50);
        ref[0] = 0;
        ref[1] = 1;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        assertEquals(true, VectorAF.compare(direction, ref, Geometry.PRECISION));
        
        // right of screen points to (1, 0, 0)
        direction = mCamera.getRayDirection(100, 50);
        ref[0] = 1;
        ref[1] = 0;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        assertEquals(true, VectorAF.compare(direction, ref, Geometry.PRECISION));
        
        // mid top of screen points to (0.7, -0.7, 1)
        // (near plane distance is 1)
        direction = mCamera.getRayDirection(50, 0);
        ref[0] = 1;
        ref[1] = 1;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        ref[2] = -1;
        VectorAF.normalize(ref);
        assertEquals(true, VectorAF.compare(direction, ref, Geometry.PRECISION));
        
        // mid bottom of screen points to (0.7, 0.7, -1)
        // (near plane distance is 1)
        direction = mCamera.getRayDirection(50, 100);
        ref[0] = 1;
        ref[1] = 1;
        ref[2] = 0;
        ref[3] = 0;
        VectorAF.normalize(ref);
        ref[2] = 1;
        VectorAF.normalize(ref);
        assertEquals(true, VectorAF.compare(direction, ref, Geometry.PRECISION));
        
    }

    private static class DebugPerspectiveCamera extends PerspectiveCamera {
        
        public DebugPerspectiveCamera() {
            super(false);
        }
        
        // make this function visible
        public void setScreenDimensions(int x, int y) {
            super.setScreenDimensions(x, y);
        }

        @Override
        public float[] getRayDirection(int x, int y) {
            return super.getRayDirection(x, y);
        } 
        
    }
}
