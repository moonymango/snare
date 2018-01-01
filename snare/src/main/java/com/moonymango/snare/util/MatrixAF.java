package com.moonymango.snare.util;

import android.opengl.Matrix;


/**
 * Allocation free matrix operations.
 * Not thread-safe!
 */
@SuppressWarnings("deprecation")
public class MatrixAF extends Matrix {

    /** We only use 4x4 matrices */
    public static final int MATRIX_ELEM_CNT = 16;
    
    private static final float[] sTmpMatA = new float[MATRIX_ELEM_CNT];
    private static final float[] sTmpMatB = new float[MATRIX_ELEM_CNT];
    private static final float[] sTmpVecA = new float[4];
    private static final float[] sTmpVecB = new float[4];
    
    /**
     * Rotates a matrix in place.
     * @param m
     * @param mOffset
     * @param a angle in degrees.
     * @param x
     * @param y
     * @param z
     */
    public static void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {
        setRotateM(sTmpMatA, 0, a, x, y, z);
        multiplyMM(sTmpMatB, 0, m, mOffset, sTmpMatA, 0);
        for (int i = 0; i < MATRIX_ELEM_CNT; i++) m[i + mOffset] = sTmpMatB[i];
    }
    
    /**
     * Rotates a matrix. In contrast to rotateM() the new rotation is used as
     * left hand side operator for the multiplication. That means this rotation is
     * the last of all transformations represented by matrix m.
     * @param m
     * @param offset
     * @param a angle in degrees.
     * @param x
     * @param y
     * @param z
     */
    public static void lhsRotateM(float[] m, int offset, float a, float x, float y, float z)
    {
        setRotateM(sTmpMatA, 0, a, x, y, z);
        multiplyMM(sTmpMatB, 0, sTmpMatA, 0, m, offset);
        for (int i = 0; i < MATRIX_ELEM_CNT; i++) m[i + offset] = sTmpMatB[i];
    }

    /**
     * Returns rotated matrix in a tmp array.
     * In contrast to rotateM() the new rotation is used as
     * left hand side operator for the multiplication. That means this rotation is
     * the last of all transformations represented by matrix m.
     * @param m
     * @param offset
     * @param a angle in degrees.
     * @param x
     * @param y
     * @param z
     */
    public static float[] lhsRotateM2(float[] m, int offset, float a, float x, float y, float z)
    {
        setRotateM(sTmpMatA, 0, a, x, y, z);
        multiplyMM(sTmpMatB, 0, sTmpMatA, 0, m, offset);
        return sTmpMatB;
    }

    /**
     * Rotate a matrix.
     * @param rm
     * @param rmOffset
     * @param m
     * @param mOffset
     * @param a Angle in degrees.
     * @param x
     * @param y
     * @param z
     */
    public static void rotateM(float[] rm, int rmOffset, float[] m, 
            int mOffset, float a, float x, float y, float z) {
        setRotateM(sTmpMatA, 0, a, x, y, z);
        multiplyMM(m, rmOffset, m, mOffset, sTmpMatA, 0);
    }
    
    /** 
     * Allocation free matrix invert function, taken from:
     * https://github.com/CyanogenMod/android_frameworks_base/blob/cm-14.1/opengl/java/android/opengl/Matrix.java
     *
        * Copyright (C) 2007 The Android Open Source Project
        *
        * Licensed under the Apache License, Version 2.0 (the "License");
        * you may not use this file except in compliance with the License.
        * You may obtain a copy of the License at
        *
        *      http://www.apache.org/licenses/LICENSE-2.0
        *
        * Unless required by applicable law or agreed to in writing, software
        * distributed under the License is distributed on an "AS IS" BASIS,
        * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        * See the License for the specific language governing permissions and
        * limitations under the License.
     */
    public static boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset) {    
        // Invert a 4 x 4 matrix using Cramer's Rule
        
        // transpose matrix
        final float src0  = m[mOffset];
        final float src4  = m[mOffset +  1];
        final float src8  = m[mOffset +  2];
        final float src12 = m[mOffset +  3];
        
        final float src1  = m[mOffset +  4];
        final float src5  = m[mOffset +  5];
        final float src9  = m[mOffset +  6];
        final float src13 = m[mOffset +  7];
        
        final float src2  = m[mOffset +  8];
        final float src6  = m[mOffset +  9];
        final float src10 = m[mOffset + 10];
        final float src14 = m[mOffset + 11];
        
        final float src3  = m[mOffset + 12];
        final float src7  = m[mOffset + 13];
        final float src11 = m[mOffset + 14];
        final float src15 = m[mOffset + 15];
        
        // calculate pairs for first 8 elements (cofactors)
        final float atmp0  = src10 * src15;
        final float atmp1  = src11 * src14;
        final float atmp2  = src9  * src15;
        final float atmp3  = src11 * src13;
        final float atmp4  = src9  * src14;
        final float atmp5  = src10 * src13;
        final float atmp6  = src8  * src15;
        final float atmp7  = src11 * src12;
        final float atmp8  = src8  * src14;
        final float atmp9  = src10 * src12;
        final float atmp10 = src8  * src13;
        final float atmp11 = src9  * src12;
        
        // calculate first 8 elements (cofactors)
        final float dst0  = (atmp0 * src5 + atmp3 * src6 + atmp4  * src7)
                          - (atmp1 * src5 + atmp2 * src6 + atmp5  * src7);
        final float dst1  = (atmp1 * src4 + atmp6 * src6 + atmp9  * src7)
                          - (atmp0 * src4 + atmp7 * src6 + atmp8  * src7);
        final float dst2  = (atmp2 * src4 + atmp7 * src5 + atmp10 * src7)
                          - (atmp3 * src4 + atmp6 * src5 + atmp11 * src7);
        final float dst3  = (atmp5 * src4 + atmp8 * src5 + atmp11 * src6)
                          - (atmp4 * src4 + atmp9 * src5 + atmp10 * src6);
        final float dst4  = (atmp1 * src1 + atmp2 * src2 + atmp5  * src3)
                          - (atmp0 * src1 + atmp3 * src2 + atmp4  * src3);
        final float dst5  = (atmp0 * src0 + atmp7 * src2 + atmp8  * src3)
                          - (atmp1 * src0 + atmp6 * src2 + atmp9  * src3);
        final float dst6  = (atmp3 * src0 + atmp6 * src1 + atmp11 * src3)
                          - (atmp2 * src0 + atmp7 * src1 + atmp10 * src3);
        final float dst7  = (atmp4 * src0 + atmp9 * src1 + atmp10 * src2)
                          - (atmp5 * src0 + atmp8 * src1 + atmp11 * src2);
        
        // calculate pairs for second 8 elements (cofactors)
        final float btmp0  = src2 * src7;
        final float btmp1  = src3 * src6;
        final float btmp2  = src1 * src7;
        final float btmp3  = src3 * src5;
        final float btmp4  = src1 * src6;
        final float btmp5  = src2 * src5;
        final float btmp6  = src0 * src7;
        final float btmp7  = src3 * src4;
        final float btmp8  = src0 * src6;
        final float btmp9  = src2 * src4;
        final float btmp10 = src0 * src5;
        final float btmp11 = src1 * src4;
        
        // calculate second 8 elements (cofactors)
        final float dst8  = (btmp0  * src13 + btmp3  * src14 + btmp4  * src15)
                          - (btmp1  * src13 + btmp2  * src14 + btmp5  * src15);
        final float dst9  = (btmp1  * src12 + btmp6  * src14 + btmp9  * src15)
                          - (btmp0  * src12 + btmp7  * src14 + btmp8  * src15);
        final float dst10 = (btmp2  * src12 + btmp7  * src13 + btmp10 * src15)
                          - (btmp3  * src12 + btmp6  * src13 + btmp11 * src15);
        final float dst11 = (btmp5  * src12 + btmp8  * src13 + btmp11 * src14)
                          - (btmp4  * src12 + btmp9  * src13 + btmp10 * src14);
        final float dst12 = (btmp2  * src10 + btmp5  * src11 + btmp1  * src9 )
                          - (btmp4  * src11 + btmp0  * src9  + btmp3  * src10);
        final float dst13 = (btmp8  * src11 + btmp0  * src8  + btmp7  * src10)
                          - (btmp6  * src10 + btmp9  * src11 + btmp1  * src8 );
        final float dst14 = (btmp6  * src9  + btmp11 * src11 + btmp3  * src8 )
                          - (btmp10 * src11 + btmp2  * src8  + btmp7  * src9 );
        final float dst15 = (btmp10 * src10 + btmp4  * src8  + btmp9  * src9 )
                          - (btmp8  * src9  + btmp11 * src10 + btmp5  * src8 );
        
        // calculate determinant
        final float det =
                src0 * dst0 + src1 * dst1 + src2 * dst2 + src3 * dst3;
        
        if (det == 0.0f) {
            throw new IllegalStateException("unable to invert matrix");
        }
        
        // calculate matrix inverse
        final float invdet = 1.0f / det;
        mInv[     mInvOffset] = dst0  * invdet;
        mInv[ 1 + mInvOffset] = dst1  * invdet;
        mInv[ 2 + mInvOffset] = dst2  * invdet;
        mInv[ 3 + mInvOffset] = dst3  * invdet;
        
        mInv[ 4 + mInvOffset] = dst4  * invdet;
        mInv[ 5 + mInvOffset] = dst5  * invdet;
        mInv[ 6 + mInvOffset] = dst6  * invdet;
        mInv[ 7 + mInvOffset] = dst7  * invdet;
        
        mInv[ 8 + mInvOffset] = dst8  * invdet;
        mInv[ 9 + mInvOffset] = dst9  * invdet;
        mInv[10 + mInvOffset] = dst10 * invdet;
        mInv[11 + mInvOffset] = dst11 * invdet;
        
        mInv[12 + mInvOffset] = dst12 * invdet;
        mInv[13 + mInvOffset] = dst13 * invdet;
        mInv[14 + mInvOffset] = dst14 * invdet;
        mInv[15 + mInvOffset] = dst15 * invdet;
        
        return true;
    }
    
    /**
     * Multiplies 4x4 matrix and a vec4. The result is copied back into v.
     * @param m
     * @param v 
     */
    public static void multiplyMV(float[] m, float[]v) {
        multiplyMV(sTmpVecA, 0, m, 0, v, 0);
        v[0] = sTmpVecA[0];
        v[1] = sTmpVecA[1];
        v[2] = sTmpVecA[2];
        v[3] = sTmpVecA[3];
    }
    
    /**
     * Ortho-normalize matrix columns based on z component, i.e.
     * the vector formed by 3rd column of matrix will keep its 
     * direction while x and y component gets adjusted.
     * @param mat
     */
    public static void orthoNormalize(float[] mat) {
        final float[] up = sTmpVecA;
        final float[] forward = sTmpVecB;
        // up
        up[0] = mat[4];
        up[1] = mat[5];
        up[2] = mat[6];
        up[3] = mat[7];
        // forward
        forward[0] = mat[8];
        forward[1] = mat[9];
        forward[2] = mat[10];
        forward[3] = mat[11];
        // get left from up and forward
        VectorAF.cross(mat, up, forward);
        VectorAF.normalize(mat);
        // get up from forward and left
        VectorAF.cross(up, forward, mat);
        VectorAF.normalize(up);
        // normalize forward
        VectorAF.normalize(forward);
        
        // copy results back
        mat[4] = up[0]; 
        mat[5] = up[1];
        mat[6] = up[2];
        mat[7] = up[3];
        mat[8] = forward[0]; 
        mat[9] = forward[1];
        mat[10] = forward[2];
        mat[11] = forward[3];
        
    }
    
    /**
     * Sets up complete local-to-world coordinate transformation.
     */
    public static void local2World(float[] toWorld, float[] position, 
            float[] scale, float[] rotation) {
        MatrixAF.setIdentityM(toWorld, 0);
        MatrixAF.translateM(toWorld, 0, position[0], position[1], position[2]);
        MatrixAF.multiplyMM(sTmpMatA, 0, toWorld, 0, rotation, 0);
        MatrixAF.scaleM(toWorld, 0, sTmpMatA, 0, scale[0], scale[1], scale[2]);
    }
    
}
