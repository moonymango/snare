package stachelsau.snare.util;


/**
 * Allocation free quaternion operations. Quaternions are represented as float 
 * vectors 
 * like this: {w, x, y, z}
 * Note: NOT thread safe
 */
public class QuaternionAF {
    
    public static final int W = 0;
    public static final int X = 1;
    public static final int Y = 2;
    public static final int Z = 3;
    
    private static final float[] sTmpVecA = new float[4];
    private static final float[] sTmpVecB = new float[4];
    private static final float[] sTmpMat = new float[16];
    
    /**
     * Sets identity quaternion. 
     */
    public static void setIdentity(float[] q) {
        q[0] = 1;
        q[1] = 0;
        q[2] = 0;
        q[3] = 0;
    }
    
    /**
     * Normalize quaternion.
     * @param q Quaternion
     * @return Magnitude.
     */
    public static float normalize(float[] q) {    
        float mag = (float) Math.sqrt(q[W]*q[W] + q[X]*q[X] + q[Y]*q[Y] + q[Z]*q[Z]);
        if (mag > Geometry.PRECISION && Math.abs(mag - 1.0f) > Geometry.PRECISION) {
            for (int i = 0; i < 4; i++) {
                q[i] /= mag;
            }  
        } 
        return mag;
    }
    
    /**
     * Extracts rotation axis and angle from a unit length quaternion. 
     * @param q Quaternion.
     * @return axisAngle (reference to internal float[], so don't keep that 
     * reference)
     */
    public static float[] toAxisAngle(float[] q) {
        float scale = (float) Math.sqrt(q[X] * q[X] + q[Y] * q[Y] + q[Z] * q[Z]);
        if (scale < Geometry.PRECISION) {
            // no rotation axis defined, e.g. in a identity quaternion, 
            // so just return rotation about x with zero angle.
            sTmpVecA[0] = 1;
            sTmpVecA[1] = 0;
            sTmpVecA[2] = 0;
            sTmpVecA[4] = 0;
        } else {
            sTmpVecA[0] = q[X] / scale;
            sTmpVecA[1] = q[Y] / scale;
            sTmpVecA[2] = q[Z] / scale;
            sTmpVecA[4] = (float) Math.acos(q[W]) * 2.0f / Geometry.RAD360 * 360;
        }
        return sTmpVecA;
    }
    
    /**
     * Creates unit length quaternion from axis + angle.
     * @param angle Angle in degrees.
     * @return quaternion (reference to internal float[], so don't keep that 
     * reference)
     */
    public static float[] fromAxisAngle(float x, float y, float z, 
            float angle) {
        final float _angle = angle / 360 * Geometry.RAD360 * 0.5f;
        final float sinAngle = (float) Math.sin(_angle);
        sTmpVecA[W] = (float) Math.cos(_angle);
        sTmpVecA[X] = x * sinAngle;
        sTmpVecA[Y] = y * sinAngle;
        sTmpVecA[Z] = z * sinAngle;
        return sTmpVecA;
    }
    
    /**
     * Get the corresponding matrix.
     * @param q Quaternion. Must be of unit length!
     * @return Matrix. (reference to internal float[], so don't keep that 
     * reference)
     */
    public static float[] toMatrix(float[] q) {
        /*
        [ 1 - 2y2 - 2z2     2xy - 2wz       2xz + 2wy
          2xy + 2wz         1 - 2x2 - 2z2   2yz - 2wx
          2xz - 2wy         2yz + 2wx       1 - 2x2 - 2y2 ]
        */
        
        final float _2x2 = 2*q[X]*q[X];
        final float _2y2 = 2*q[Y]*q[Y];
        final float _2z2 = 2*q[Z]*q[Z];
        
        final float _2wx = 2*q[W]*q[X];
        final float _2wy = 2*q[W]*q[Y];
        final float _2wz = 2*q[W]*q[Z];
        final float _2xy = 2*q[X]*q[Y];
        final float _2xz = 2*q[X]*q[Z];
        final float _2yz = 2*q[Y]*q[Z];
        
        sTmpMat[0] = 1 - _2y2 - _2z2;
        sTmpMat[1] = _2xy + _2wz;
        sTmpMat[2] = _2xz - _2wy;
        sTmpMat[3] = 0;
        
        sTmpMat[4] = _2xy - _2wz;
        sTmpMat[5] = 1 - _2x2 - _2z2;
        sTmpMat[6] = _2yz + _2wx;
        sTmpMat[7] = 0;
        
        sTmpMat[8] = _2xz + _2wy;
        sTmpMat[9] = _2yz - _2wx;
        sTmpMat[10] = 1 - _2x2 - _2y2;
        sTmpMat[11] = 0;
        
        sTmpMat[12] = 0;
        sTmpMat[13] = 0;
        sTmpMat[14] = 0;
        sTmpMat[15] = 1;
        
        return sTmpMat;
    }
    
    /**
     * Get corresponding quaternion from rotation matrix (matrix must be
     * orthonormal)
     * 
     * (http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/)
     * @param mat
     * @return Quaternion.
     */
    public static float[] fromMatrix(float[] mat) {
        float tr = mat[0] + mat[5] + mat[10];

        if (tr > 0) { 
          final float S = (float) (Math.sqrt(tr+1.0) * 2); // S=4*qw 
          sTmpVecA[W] = (float) (0.25 * S);
          sTmpVecA[X] = (mat[6] - mat[9]) / S;
          sTmpVecA[Y] = (mat[8] - mat[2]) / S; 
          sTmpVecA[Z] = (mat[1] - mat[4]) / S; 
        } else if ((mat[0] > mat[5])&(mat[0] > mat[10])) { 
          final float S = (float) (Math.sqrt(1.0 + mat[0] - mat[5] - mat[10]) * 2); // S=4*qx 
          sTmpVecA[W] = (mat[6] - mat[9]) / S;
          sTmpVecA[X] = (float) (0.25 * S);
          sTmpVecA[Y] = (mat[4] + mat[1]) / S; 
          sTmpVecA[Z] = (mat[8] + mat[2]) / S; 
        } else if (mat[5] > mat[10]) { 
          float S = (float) (Math.sqrt(1.0 + mat[5] - mat[0] - mat[10]) * 2); // S=4*qy
          sTmpVecA[W] = (mat[8] - mat[2]) / S;
          sTmpVecA[X] = (mat[4] + mat[1]) / S; 
          sTmpVecA[Y] = (float) (0.25 * S);
          sTmpVecA[Z] = (mat[9] + mat[6]) / S; 
        } else { 
          float S = (float) (Math.sqrt(1.0 + mat[10] - mat[0] - mat[5]) * 2); // S=4*qz
          sTmpVecA[W] = (mat[1] - mat[4]) / S;
          sTmpVecA[X] = (mat[8] + mat[2]) / S;
          sTmpVecA[Y] = (mat[9] + mat[6]) / S;
          sTmpVecA[Z] = (float) (0.25 * S);
        }
        return sTmpVecA;
    }
    
    /**
     * Multiplies two quaternions (q1 * q2).
     * @param q1
     * @param q2
     * @param result
     */
    public static void mul(float[] result, float[] q1, float[] q2) {
        result[W] = q1[W]* q2[W] - q1[X]* q2[X] - q1[Y]* q2[Y] - q1[Z]* q2[Z];
        result[X] = q1[W]* q2[X] + q1[X]* q2[W] + q1[Y]* q2[Z] - q1[Z]* q2[Y];
        result[Y] = q1[W]* q2[Y] - q1[X]* q2[Z] + q1[Y]* q2[W] + q1[Z]* q2[X];
        result[Z] = q1[W]* q2[Z] + q1[X]* q2[Y] - q1[Y]* q2[X] + q1[Z]* q2[W];
    }
    
    
    /**
     * Rotates quaternion q by axis and angle. The new rotation will be the
     * left hand side operator in multiplication, so this rotation is the last of
     * all rotations represented by q.
     * @param angle Angle in degrees. 
     */
    public static void lhsRotateQ(float[] q, float x, float y, float z, 
            float angle) {
        final float[] qr = fromAxisAngle(x, y, z, angle);
        // note: qr points to sTmpVecA now (see fromAxisAngle())
        mul(sTmpVecB, qr, q);
        for (int i = 0; i < 4; i++) {
            q[i] = sTmpVecB[i];
        }
    }
    
    public static float dot(float[] q0, float[] q1) {
        return q0[0]*q1[0] + q0[1]*q1[1] + q0[2]*q1[2] + q0[3]*q1[3];  
    }
    
    /**
     * Angle between two quarternions.
     * @param q0
     * @param q1
     * @return Angle in degrees.
     */
    public static float angle(float[] q0, float[] q1) {
        final float result = (float) (2*Math.acos(Math.abs(dot(q0, q1))));
        return Geometry.toDegree(result);
    }
    
    /**
     * Spherical linear interpolation between two quaternions.
     * @param q0
     * @param q1
     * @param t [0..1]
     * @param shortestPath Force shortest path.
     * @return
     */
    public static float[] slerp(float[] q0, float[] q1, float t, 
            boolean shortestPath) {
        float cosTheta = dot(q0, q1);
        final boolean flip = cosTheta < 0 && shortestPath; // flip for shortest path
        cosTheta = flip ? -cosTheta : cosTheta;
        final float theta = (float) Math.acos(cosTheta);
        final float sin = (float) Math.sin(theta);
        final float f0 = (float) (Math.sin(theta*(1-t))/sin);
        float f1 = (float) (Math.sin(theta*t)/sin); 
        f1 = flip ? -f1 : f1;
        
        sTmpVecA[W] = f0*q0[W] + f1*q1[W];
        sTmpVecA[X] = f0*q0[X] + f1*q1[X];
        sTmpVecA[Y] = f0*q0[Y] + f1*q1[Y];
        sTmpVecA[Z] = f0*q0[Z] + f1*q1[Z];
        return sTmpVecA;
    }
}
