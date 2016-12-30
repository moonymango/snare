package stachelsau.snare.util;


public class MatrixStack {

    // size of 4x4 matrix
    private static final int MATRIX_ELEMENTS = 16; 
    
    /** Stores the matrices on stack. */
    private final float[][] mStack;
    /** Stores the matrix products. */ 
    private final float[][] mProducts;
    private final int mSize;
    private final boolean mCloneResults;
    private int mPos;
    private boolean mIsEmpty = true;
    
    private final float[] mInvTranspose = new float[MATRIX_ELEMENTS];
    private final float[] mInv = new float[MATRIX_ELEMENTS];
    private boolean mInvValid;
    
        
    public MatrixStack(int size, boolean cloneResults) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1.");
        }
        
        mSize = size;
        mCloneResults = cloneResults;
        mProducts = new float[size][];
        mStack = new float[size][];
        for (int i = 0; i < size; i++) {
            mProducts[i] = new float[MATRIX_ELEMENTS];
            mStack[i] = new float[MATRIX_ELEMENTS];
        }    
    }
    
    public MatrixStack(int size) {
        this(size, false);
    }
    
    public void reset() {
        mPos = 0;
        mIsEmpty = true;
    }
    
    /**
     * Gets the product of all matrices on stack. 
     * @return Direct reference. DO NOT MODIFY!
     */
    public float[] getProduct() {
        if (mIsEmpty) {
            return null;
        }
        if (mCloneResults) {
            return mProducts[mPos].clone();
        }
        return mProducts[mPos];
    }
    
    /**
     * Gets inverse of matrix stack's product.
     * @return Direct reference. DO NOT MODIFY!
     */
    public float[] getInv() {
        if (mIsEmpty) {
            return null;
        }
        if (!mInvValid) {
            invert();
        }
        if (mCloneResults) {
            return mInv.clone();
        }
        return mInv;
    }
    
    /**
     * Returns transposed inverse of the stack's top matrix. This is to be used
     * as view space transformation for normals.
     * @return Direct reference to inverse matrix. DO NOT MODIFY!
     */
    public float[] getInvTranspose() {
        if (mIsEmpty) {
            return null;
        }
        if (!mInvValid) {
            invert();
        }
        if (mCloneResults) {
            return mInvTranspose.clone();
        }
        return mInvTranspose;
    }
    
    /**
     * Copies stack product to destination.
     * @param dst
     */
    public void copyProduct(float[] dst) {
        if (mIsEmpty) {
            return;
        }  
        for(int i = 0; i < MATRIX_ELEMENTS; i++) dst[i] = mProducts[mPos][i];
    }
    
    /**
     * Copies stack inverse to destination.
     * @param dst
     */
    public void copyInv(float[] dst) {
        if (mIsEmpty) {
            return;
        } 
        if (!mInvValid) {
            invert();
        }
        
        for(int i = 0; i < MATRIX_ELEMENTS; i++) dst[i] = mInv[i];
    }
    
    /**
     * Copies transposed inverse to destination.
     * @param dst
     */
    public void copyInvTranspose(float[] dst) {
        if (mIsEmpty) {
            return;
        } 
        if (!mInvValid) {
            invert();
        }
        for(int i = 0; i < MATRIX_ELEMENTS; i++) dst[i] = mInvTranspose[i];
        
    }
    
    /**
     * Returns top matrix.
     * @return Direct reference. DO NOT MODIFY!
     */
    public float[] getTop() {
        if (mIsEmpty) {
            return null;
        }
        if (mCloneResults) {
            return mStack[mPos].clone();
        }
        return mStack[mPos];
    }
    
    public boolean isEmpty() {
        return mIsEmpty;
    }
    
    public float[] pushMatrix(float[] mat) {
        if (mat.length != MATRIX_ELEMENTS) {
            throw new IllegalArgumentException("Invalid matrix.");
        }
                
        if (mIsEmpty) {
            for (int i = 0; i < MATRIX_ELEMENTS; i++) {
                mProducts[0][i] = mat[i];
            }
            mIsEmpty = false;
          
        } else {
            int newPos = mPos + 1;
            if (newPos > mSize - 1) {
                throw new IllegalStateException("Stack size exceeded.");
            }
            MatrixAF.multiplyMM(mProducts[newPos], 0, mProducts[mPos], 0, mat, 0);
            mPos = newPos;
        }
        
        for (int i = 0; i < MATRIX_ELEMENTS; i++) {
            mStack[mPos][i] = mat[i];
        }
        
        if (mCloneResults) {
            return mProducts[mPos].clone();
        }
        mInvValid = false;
        return mProducts[mPos];
    }
    
    public void popMatrix() {
        if (mPos == 0) {
            if (mIsEmpty) { 
                throw new IllegalStateException("Stack is already empty.");
            }
            mIsEmpty = true;
        } else {
            mPos--;
        }
        mInvValid = false;
    }
    
    private void invert() {
        final boolean success = MatrixAF.invertM(mInv, 0, mProducts[mPos], 0); 
        if (!success) {
            throw new IllegalStateException("Top matrix could not be inverted.");
        }
        MatrixAF.transposeM(mInvTranspose, 0, mInv, 0);
        mInvValid = true;
    }

}
