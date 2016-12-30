package stachelsau.snare.util;


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
        float f = (float)(i0) / SHIFT2; 
        return f;
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
