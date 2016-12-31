package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.opengl.ProgramObj.ILocationHolder;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.ColorWrapper.IColorSeqListener;
import com.moonymango.snare.ui.IScreenElement;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.util.MatrixAF;

/**
 * Base class for 2D widgets which handles all projection/transformation matrix
 * calculation.
 * TODO prioD: review widget class hierarchy,
 * especially AABB implementation
 */
public abstract class BaseWidget implements IScreenElement, ILocationHolder,
        IColorSeqListener, IPositionable2D {

    // ---------------------------------------------------------
    // static
    // ---------------------------------------------------------
    /* General purpose vector to avoid allocations */
    private static float[] sTmpVec = new float[4];

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    /** Complete matrix to use for drawing by derived classes*/
    protected float[] mMatrix = new float[16];
    /** Inverse of transformation matrix */
    protected float[] mTMatrixInv = new float[16];
    
    protected BlendMode mBlendMode = BlendMode.OPAQUE;
    
    private float[] mPMatrix = new float[16];
    private float[] mTMatrix = new float[16];
    private int mPosX;
    private int mPosY;
    private int mLocalOffsetX;
    private int mLocalOffsetY;
    private float mScaleX = 1;
    private float mScaleY = 1;
    private float mAngle;
    protected float[] mColor = { 1, 1, 1, 1 };
    private boolean mIsAttached = false;
    private PlayerGameView mView;
    private boolean mIsVisible = true;
    private int mAABBLeft;
    private int mAABBRight;
    private int mAABBTop;
    private int mAABBBottom;
    private boolean mAABBValid;
    private ColorWrapper mColorPalette;    

    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    
    @Override
    public boolean containsCoord(int x, int y) {
        // transform screen coordinates back to local coordinates and check 
        // rectangle's area     
        sTmpVec[0] = x;
        sTmpVec[1] = y;
        sTmpVec[2] = 0;
        sTmpVec[3] = 1;
        MatrixAF.multiplyMV(mTMatrixInv, sTmpVec);
        
        if (sTmpVec[0] < getLocalLeft() || sTmpVec[0] > getLocalRight()) 
            return false; // x out of rectangle area
        if (sTmpVec[1] < getLocalBottom() || sTmpVec[1] > getLocalTop()) 
            return false; // y out of rectangle area
        return true;
    }
    
    
    public void onAttachToScreen(PlayerGameView view, int screenWidth,
            int screenHeight) {
        if (mIsAttached && view != mView) {
            throw new IllegalStateException(
                    "Shape is already attached to a view.");
        }
        mIsAttached = true;
        mView = view;
        updateProjection(screenWidth, screenHeight);
        updateTransformation();
    }

    public void onDetachFromScreen() {
        if (!mIsAttached) {
            throw new IllegalStateException("Shape is not attached to a view.");
        }
        mIsAttached = false;
        mView = null;
    }

    public PlayerGameView getView() {
        return mView;
    }

    /**
     * Sets position: lower left corner of screen is (0, 0).
     * 
     * @param x
     * @param y
     */
    public BaseWidget setPosition(int x, int y) {
        mPosX = x;
        mPosY = y;
        updateTransformation();
        return this;
    }

    public int getPositionX() {
        return mPosX;
    }

    public int getPositionY() {
        return mPosY;
    }

    public abstract BaseWidget setPositionAlignment(PositionAlignment a);

    /**
     * Sets offset for local translation, which is performed prior to any other
     * transformation. Transformation order: local translation > scale > rotate
     * > translate
     * 
     * @param x
     * @param y
     */
    protected void setLocalOffset(int x, int y) {
        mLocalOffsetX = x;
        mLocalOffsetY = y;
        updateTransformation();
    }

    /**
     * Sets angle of widget.
     * @param angle in degress.
     * @return
     */
    public BaseWidget setAngle(float angle) {
        mAngle = angle;
        updateTransformation();
        return this;
    }

    public float getAngle() {
        return mAngle;
    }

    /** Sets scale transformation. */
    public BaseWidget setScale(float x, float y) {
        mScaleX = x;
        mScaleY = y;
        updateTransformation();
        return this;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }
    
    /**
     * Sets scale in terms of width/height on screen.
     * @param width
     * @param height
     * @return
     */
    public BaseWidget setSize(int width, int height) {
        if (width < 1 || height < 1)
            throw new IllegalArgumentException("Dimensions must not be less than 0.");
        
        final float h = Math.abs(getLocalTop() - getLocalBottom());
        final float w = Math.abs(getLocalRight() - getLocalLeft());
        
        final float scaleX = ((float) width) / w;
        final float scaleY = ((float) height) / h;
        this.setScale(scaleX, scaleY);
        return this;
    }

    /** Width of unrotated widget. */
    public abstract int getWidth();

    /** Height of unrotated widget. */
    public abstract int getHeight();
    
    public BaseWidget setBlendMode(BlendMode mode) {
        mBlendMode = mode != null ? mode : BlendMode.OPAQUE;
        return this;
    }

    public BaseWidget setColor(float r, float g, float b, float a) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        mColor[3] = a;
        return this;
    }
    
    public BaseWidget setColorPalette(ColorWrapper cp) {
        if (mColorPalette != null) {
            mColorPalette.removeListener(this);
        }
        mColorPalette = cp;
        cp.addListener(this);
        return this;
    }

    public void onColorChange(ColorWrapper cp) {
        final float[] c = cp.getActualColor();
        mColor[0] = c[0];
        mColor[1] = c[1];
        mColor[2] = c[2];
        mColor[3] = c[3];
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    public void show(boolean visible) {
        mIsVisible = visible;
    }

    public boolean isAttached() {
        return mIsAttached;
    }

    public boolean onTouchEvent(ITouchEvent e) {
        return false;
    }

    private void updateTransformation() {
        MatrixAF.setIdentityM(mTMatrix, 0);
        MatrixAF.translateM(mTMatrix, 0, mPosX, mPosY, 0);
        MatrixAF.rotateM(mTMatrix, 0, mAngle, 0, 0, 1);
        MatrixAF.scaleM(mTMatrix, 0, mScaleX, mScaleY, 1);
        MatrixAF.translateM(mTMatrix, 0, mLocalOffsetX, mLocalOffsetY, 0);
        MatrixAF.multiplyMM(mMatrix, 0, mPMatrix, 0, mTMatrix, 0);
        MatrixAF.invertM(mTMatrixInv, 0, mTMatrix, 0);

        mAABBValid = false;
    }

    private void updateProjection(int screenWidth, int screenHeight) {
        // lower left corner of screen is (0, 0)
        MatrixAF.orthoM(mPMatrix, 0, 0, screenWidth, 0, screenHeight, 0, 1f);
        MatrixAF.multiplyMM(mMatrix, 0, mPMatrix, 0, mTMatrix, 0);
    }

    protected abstract float getLocalLeft();

    protected abstract float getLocalRight();

    protected abstract float getLocalTop();

    protected abstract float getLocalBottom();

    public int getAABBLeft() {
        if (!mAABBValid) {
            calcAABB();
        }
        return mAABBLeft;
    }

    public int getAABBRight() {
        if (!mAABBValid) {
            calcAABB();
        }
        return mAABBRight;
    }

    public int getAABBTop() {
        if (!mAABBValid) {
            calcAABB();
        }
        return mAABBTop;
    }

    public int getAABBBottom() {
        if (!mAABBValid) {
            calcAABB();
        }
        return mAABBBottom;
    }

    private void calcAABB() {
        // transform local bounding box
        // top left
        sTmpVec[0] = getLocalLeft();
        sTmpVec[1] = getLocalTop();
        sTmpVec[2] = 0;
        sTmpVec[3] = 1;
        MatrixAF.multiplyMV(mTMatrix, sTmpVec);
        int tlX = (int) sTmpVec[0];
        int tlY = (int) sTmpVec[1];

        // top right
        sTmpVec[0] = getLocalRight();
        sTmpVec[1] = getLocalTop();
        sTmpVec[2] = 0;
        sTmpVec[3] = 1;
        MatrixAF.multiplyMV(mTMatrix, sTmpVec);
        int trX = (int) sTmpVec[0];
        int trY = (int) sTmpVec[1];

        // bottom left
        sTmpVec[0] = getLocalLeft();
        sTmpVec[1] = getLocalBottom();
        sTmpVec[2] = 0;
        sTmpVec[3] = 1;
        MatrixAF.multiplyMV(mTMatrix, sTmpVec);
        int blX = (int) sTmpVec[0];
        int blY = (int) sTmpVec[1];

        // bottom right
        sTmpVec[0] = getLocalRight();
        sTmpVec[1] = getLocalBottom();
        sTmpVec[2] = 0;
        sTmpVec[3] = 1;
        MatrixAF.multiplyMV(mTMatrix, sTmpVec);
        int brX = (int) sTmpVec[0];
        int brY = (int) sTmpVec[1];

        mAABBLeft = Math.min(tlX, Math.min(trX, Math.min(blX, brX)));
        mAABBRight = Math.max(tlX, Math.max(trX, Math.max(blX, brX)));
        mAABBTop = Math.max(tlY, Math.max(trY, Math.max(blY, brY)));
        mAABBBottom = Math.min(tlY, Math.min(trY, Math.min(blY, brY)));

        mAABBValid = true;
    }

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
    
    public static enum BlendMode {
        OPAQUE,
        TRANSPARENT,
        REPLACE
    }
    
    /**
     * Defines alignment of widget to its position.
     */
    public static enum PositionAlignment {
        CENTERED_XY, 
        LEFT_X_TOP_Y, 
        RIGHT_X_TOP_Y, 
        LEFT_X_BOTTOM_Y, 
        RIGHT_X_BOTTOM_Y, 
        LEFT_X_CENTERED_Y, 
        RIGHT_X_CENTERED_Y
    }
}
