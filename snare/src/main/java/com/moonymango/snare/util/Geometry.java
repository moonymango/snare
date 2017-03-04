package com.moonymango.snare.util;


public class Geometry {
    
    public static final float PI = (float) Math.PI;
    public static final float RAD360 = 2 * PI;
    public static final float RAD90 = PI/2;
    public static final float RAD270 = 3*PI/2;
    public static final float RAD180 = PI;
    public static float PRECISION = 0.0001f;
    
    private static final int SHIFT2 = 1 << Integer.SIZE-1;
    private static final float[] mVec0 = new float[4];
    private static final float[] mVec1 = new float[4];
    private static final float[] mVec2 = new float[4];
    
    public static float toRadian(float degree) {
        return RAD360*degree/360;
    }
    
    public static float toDegree(float radian) {
        return 360*radian/RAD360;
    }
    
    /**
     * Negative power of two. 
     * @param p In range [0..31]
     * @return 2^(-p)
     */
    public static float negPow2(int p) {
        int i0 = SHIFT2 >> p;
        return (float)(i0) / SHIFT2;
    }

    /**
     * Compares two floats using Geometry.PRECISION
     * @param a Float
     * @param b Another float
     * @return true or false
     */
    public static boolean floatEquals(float a, float b)
    {
        float diff = a > b ? a - b : b - a;
        return diff < PRECISION;
    }

    /**
     * Returns distance between a plane and a point.
     * @param plane
     * @param point
     * @return
     */
    public static float planeDistance(float[] plane, float[] point) {
        return plane[0]*point[0] + plane[1]*point[1] + plane[2]*point[2] +
                plane[3];
    }

    /**
     * Intersection of a plane and a line. The line is represented by p(t) = s + t*v
     * @param p         Plane
     * @param s         Origin point of the line.
     * @param v         Direction vector of the line.
     * @param result    Intersection coordinates will be copied here if non-null reference is given.
     * @return t for intersection point. NaN in case of no intersection.
     *              If line is in plane, s is chosen as intersection point
     */
    public static float planeIntersection(float[] p, float[] s, float[] v, float[] result)
    {
        final float dotNV = p[0]*v[0] + p[1]*v[1] + p[2]*v[2];
        final float dotNS = p[0]*s[0] + p[1]*s[1] + p[2]*s[2];
        float d = dotNS + p[3];

        if (floatEquals(dotNV, 0))
        {
            // line is parallel to plane, check if origin is in the plane
            boolean inPlane = d < 0 ? d > -PRECISION : d < PRECISION;
            d = inPlane ? 0 : Float.NaN;
            if (d == 0 && result != null)
                System.arraycopy(s, 0, result, 0, s.length);

            return d;
        }

        // otherwise calculate intersection point
        final float t = -d/dotNV;
        if (result != null)
        {
            result[0] = s[0] + t * v[0];
            result[1] = s[1] + t * v[1];
            result[2] = s[2] + t * v[2];
        }
        return t;
    }

    /**
     * Returns plane specified by three points.
     * @param p0
     * @param p1
     * @param p2
     * @return
     */
    public static float[] toPlane(float[] p0, float[] p1, float[] p2)
    {
        VectorAF.subtract(mVec0, p1, p0);
        VectorAF.subtract(mVec1, p2, p0);
        VectorAF.cross(mVec2, mVec1, mVec0);
        VectorAF.normalize(mVec2);
        mVec2[3] = -VectorAF.dot(mVec2, p0);
        return mVec2;
    }
    
    /**
     * Returns plane specified by two points and origin.
     * @param p0
     * @param p1
     * @return
     */
    public static float[] toPlane(float[] p0, float[] p1)
    {
        mVec2[0] = 0;
        mVec2[1] = 0;
        mVec2[2] = 0;
        mVec2[3] = 1;
        return toPlane(p0, p1, mVec2);
    }
    
    /**
     * Returns nearest point on specified plane to another point.
     * @param plane
     * @param p
     * @return
     */
    public static float[] nearestPoint(float[] plane, float[] p)
    {
        final float dist = planeDistance(plane, p);
        mVec0[0] = p[0] - dist*plane[0];
        mVec0[1] = p[1] - dist*plane[1];
        mVec0[2] = p[2] - dist*plane[2];
        mVec0[3] = 1;
        return mVec0;
    }
}
