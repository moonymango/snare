package com.moonymango.snare.util;


import static android.R.attr.y;

/**
 * Allocation free vector functions. Vectors always have 4 elements.
 * Vectors that represent points:       {x, y, z, 1}
 * Vectors that represent directions:   {x, y, z, 0}
 * Vectors that represent planes:       {A, B, C, D}
 * 
 * Not thread-safe! 
 */
public class VectorAF {
    
    /** All functions in this class expect 4 element vectors only. However
     * calculation are done in 3D so the 4th element is always set to 1. */ 
    public static final int VECTOR_ELEM_CNT = 4;
    public static final float PRECISION = Geometry.PRECISION;
    private static final float[] sTmpMat = new float[16];
    private static final float[] sTmpVec = new float[4];
    
    /**
     * Normalize a vector in place. Works not correctly for vectors that 
     * represent a point, since their w component is not zero!
     * @param vec
     * @return Magnitude of vector prior to normalize.
     */
    public static float normalize(float[] vec) {
        final float mag = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
        if (mag > PRECISION && Math.abs(mag - 1.0f) > PRECISION) {
            vec[0] /= mag;
            vec[1] /= mag;
            vec[2] /= mag;
            vec[3] /= mag;
        }
        return mag;
    }


    /**
     * Convenience function to calculate a normalized direction based
     * on coordinates. 
     * @param x
     * @param y
     * @param z
     * @return Temp. vector. Do not hold long living ref on this!
     */
    public static float[] normalize(float x, float y, float z) {
        sTmpVec[0] = x;
        sTmpVec[1] = y;
        sTmpVec[2] = z;
        sTmpVec[3] = 0;
        normalize(sTmpVec);
        return sTmpVec;
    }
    
    /**
     * Subtracts subtrahend from vec.
     * @param vec
     * @param subtrahend
     */
    public static void subtract(float[] diff, float[] minuend, float[] subtrahend) {
        diff[0] = minuend[0] - subtrahend[0];
        diff[1] = minuend[1] - subtrahend[1];
        diff[2] = minuend[2] - subtrahend[2];
        diff[3] = minuend[3] - subtrahend[3];
    }
    
    /**
     * Subtraction. Stores result in internal array and returns reference
     * to it. So the result is only valid directly after the call because
     * using other vector functions may overwrite the array's contents.
     * @param minuend
     * @param subtrahend
     * @return  
     */
    public static float[] subtract(float[] minuend, float[] subtrahend) {
        sTmpVec[0] = minuend[0] - subtrahend[0];
        sTmpVec[1] = minuend[1] - subtrahend[1];
        sTmpVec[2] = minuend[2] - subtrahend[2];
        sTmpVec[3] = minuend[3] - subtrahend[3];
        return sTmpVec;
    }
    
    /**
     * Calculates distance between two points.
     * @param p0
     * @param p1
     * @param squared True to left result squared (save sqrt operation).
     * @return
     */
    public static float distance(float[] p0, float[] p1, boolean squared) {
        final float[] v = subtract(p0, p1);
        float d = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        return squared ? d : (float) (Math.sqrt(d));
    }
    
    public static float dot(float[] vec1, float[] vec2) {
        return vec1[0]*vec2[0] + vec1[1]*vec2[1] + vec1[2]*vec2[2] + vec1[3]*vec2[3];  
    }
    
    public static void cross(float[] c, float[] a, float[] b) {
        c[0] = a[1]*b[2] - a[2]*b[1];
        c[1] = a[2]*b[0] - a[0]*b[2];
        c[2] = a[0]*b[1] - a[1]*b[0];
        c[3] = 0;
    }
    
    /**
     * Magnitude of a vector. The w component of the vector is not included
     * in the calculation.
     * @param vec
     * @return
     */
    public static float mag(float[] vec) {
        return (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
    }
    
    public static float mag(float x, float y, float z) {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }
    
    /**
     * Compare two vectors.
     * @param vec1
     * @param vec2
     * @param epsilon
     * @return TRUE in case of equality.
     */
    public static boolean compare(float[] vec1, float[] vec2, float epsilon) {
        if (vec1.length != vec2.length) {
            return false;
        }
        for (int i = 0; i < vec1.length; ++i) {
            final float delta = Math.abs(vec1[i] - vec2[i]);
            if (delta > epsilon) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculates point on a ray.
     * @param result
     * @param origin Ray origin.
     * @param direction Ray direction.
     * @param distance from ray origin.
     */
    public static void calcPointByRayDistance(float[] result, float[] origin, 
            float[] direction, float distance) {
        result[0] = origin[0] + distance * direction[0];
        result[1] = origin[1] + distance * direction[1];
        result[2] = origin[2] + distance * direction[2];
        result[3] = 1;
    }
    
    /**
     * Rotates a vector in place. (clockwise rotation as seen in direction of rotation
     * axis)
     * @param vec
     * @param angle in degrees
     * @param x
     * @param y
     * @param z
     */
    public static void rotateV(float[] vec, float angle, float x, float y, float z) {
        MatrixAF.setIdentityM(sTmpMat, 0);
        MatrixAF.rotateM(sTmpMat, 0, angle, x, y, z);
        MatrixAF.multiplyMV(sTmpMat, vec);
    }
    
    /**
     * Rotates vector and copies result into specified array.
     * @param result
     * @param vec
     * @param angle in degrees
     * @param x
     * @param y
     * @param z
     */
    public static void rotateV(float[] result, float[] vec, float angle, 
            float x, float y, float z) {
        MatrixAF.setIdentityM(sTmpMat, 0);
        MatrixAF.rotateM(sTmpMat, 0, angle, x, y, z);
        MatrixAF.multiplyMV(result, 0, sTmpMat, 0, vec, 0);
    }
}
