package com.moonymango.snareTest;


import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.QuaternionAF;
import com.moonymango.snare.util.VectorAF;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class GeometryTest  {
    
    private final float[] mVec0 = {1, 0, 0, 1};
    private final float[] mVec1 = {3, 0, 0, 1}; 
    
    @Test
    public void testPlanePointDistance() {
        final float d = Geometry.planeDistance(mVec0, mVec1);
        assertEquals(4, d, Geometry.PRECISION);
    }

    @Test
    public void testToPlane() 
    {
        // plane parallel to y-z 
        final float[] vec0 = new float[4];
        final float[] vec1 = new float[4];
        final float[] vec2 = new float[4];
        
        vec0[0] = 2;
        vec0[1] = 0;
        vec0[2] = 0;
        vec0[3] = 1;
        
        vec1[0] = 2;
        vec1[1] = 0;
        vec1[2] = 2;
        vec1[3] = 1;
        
        vec2[0] = 2;
        vec2[1] = 2;
        vec2[2] = 2;
        vec2[3] = 1;
        
        float[] plane = Geometry.toPlane(vec0, vec1, vec2);
        
        float d = Geometry.planeDistance(plane, vec0);
        assertEquals(0, d, Geometry.PRECISION);
        d = Geometry.planeDistance(plane, vec1);
        assertEquals(0, d, Geometry.PRECISION);
        d = Geometry.planeDistance(plane, vec2);
        assertEquals(0, d, Geometry.PRECISION);
        
        vec0[0] = 0;
        vec0[1] = 0;
        vec0[2] = 0;
        vec0[3] = 1;
        d = Math.abs(Geometry.planeDistance(plane, vec0));
        assertEquals(2, d, Geometry.PRECISION);
        
        // test plane through origin
        vec0[0] = 3;
        vec0[1] = 3;
        vec0[2] = 3;
        vec0[3] = 1;
        plane = Geometry.toPlane(vec1, vec2);
        d = Geometry.planeDistance(plane, vec0);
        assertEquals(0, d, Geometry.PRECISION);
    }

    @Test
    public void testNearestPoint()
    {
        final float[] n = Geometry.nearestPoint(mVec0, mVec1);
        final float[] exp = new float[4];
        exp[0] = -1;
        exp[1] = 0;
        exp[2] = 0;
        exp[3] = 1;
        for (int i = 0; i < 4; i++) 
        {
            assertEquals(exp[i], n[i], Geometry.PRECISION);
        }
        
    }

    @Test
    public void testVectorFunc() {
        final float[] vec1 = new float[4];
        final float[] vec2 = new float[4];
        vec1[0] = 1.5f;
        vec1[1] = 1.5f;
        vec1[2] = 1.5f;
        vec1[3] = 1.5f;
        
        vec2[0] = 1.5f;
        vec2[1] = 1.5f;
        vec2[2] = 1.5f;
        vec2[3] = 1.5f;
        assertEquals(true, VectorAF.compare(vec1, vec2, Geometry.PRECISION));
        
        vec2[0] = 5;
        assertEquals(false, VectorAF.compare(vec1, vec2, Geometry.PRECISION));
        
    }

    @Test
    public void testQuaternionToMatrix() {
        // convert matrix to quaternion and back
        final float[] mat = new float[16];
        final float[] rotVec = new float[4];
        final float[] q = new float[4];
        
        // build rotation vector
        rotVec[0] = 1;
        rotVec[1] = -2;
        rotVec[2] = -3;
        rotVec[3] = 0;
        VectorAF.normalize(rotVec);
        
        // build initial rotation matrix
        MatrixAF.setIdentityM(mat, 0);
        MatrixAF.lhsRotateM(mat, 0, 37.56f, rotVec[0], rotVec[1], rotVec[2]);
        
        // get and copy quaternion
        final float[] qq = QuaternionAF.fromMatrix(mat);
        for (int i = 0; i < 4; i++) {
            q[i] = qq[i];
        }
        
        // convert back and compare
        final float[] result = QuaternionAF.toMatrix(q);
        for (int i = 0; i < 16; i++) {
            assertEquals(mat[i], result[i], Geometry.PRECISION);
        }
    }

    @Test
    public void testQuaternionAngle() {
        final float[] q0 = new float[4];
        final float[] q1 = new float[4];
        
        float[] q = QuaternionAF.fromAxisAngle(1, 0, 0, 23);
        for (int i = 0; i < 4; i++) {
            q0[i] = q[i];
        }
        
        q = QuaternionAF.fromAxisAngle(1, 0, 0, 48);
        for (int i = 0; i < 4; i++) {
            q1[i] = q[i];
        }
        
        float angle = QuaternionAF.angle(q1, q0);
        float expected = 48-23;
        assertEquals(expected, angle, Geometry.PRECISION);
        
    }

    @Test
    public void testQuaternionSlerp() {
        final float[] q0 = new float[4];
        final float[] q1 = new float[4];
        final float[] q2 = new float[4];
        
        // 10°
        float[] q = QuaternionAF.fromAxisAngle(1, 0, 0, 10);
        for (int i = 0; i < 4; i++) {
            q0[i] = q[i];
        }
        
        // 50°
        q = QuaternionAF.fromAxisAngle(1, 0, 0, 50);
        for (int i = 0; i < 4; i++) {
            q1[i] = q[i];
        }
        
        // 50°
        q = QuaternionAF.fromAxisAngle(1, 0, 0, 30);
        for (int i = 0; i < 4; i++) {
            q2[i] = q[i];
        }
        
        // t=0 > interpolation == start (10°) 
        q = QuaternionAF.slerp(q0, q1, 0, true);
        assertEquals(true, VectorAF.compare(q0, q, Geometry.PRECISION));
        // t=1 > interpolation == end (50°)
        q = QuaternionAF.slerp(q0, q1, 1, true);
        assertEquals(true, VectorAF.compare(q1, q, Geometry.PRECISION));
        // t=0.5 > interpolation == 30°
        q = QuaternionAF.slerp(q0, q1, 0.5f, true);
        assertEquals(true, VectorAF.compare(q2, q, Geometry.PRECISION));
        
    }
}
