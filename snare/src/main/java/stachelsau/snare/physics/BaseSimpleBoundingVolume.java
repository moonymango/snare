package stachelsau.snare.physics;

import stachelsau.snare.util.MatrixAF;
import stachelsau.snare.util.VectorAF;

public abstract class BaseSimpleBoundingVolume extends BaseBoundingVolume {

    private boolean mCheckCollisions;
    private boolean centerWorldValid;
    private final float[] mCenterWorld = new float[4];
    private final float[] mCollisionPoint = new float[4];
    protected float[] mCenter = new float[4];
    
    /** general purpose vector */
    protected final float[] mS = new float[4];
    /** general purpose vector */
    protected final float[] mV = new float[4];
     
    protected BaseSimpleBoundingVolume(boolean checkCollisions) {
        mCheckCollisions = checkCollisions;
    }

    @Override
    public VolumeType getType() {
        return null;
    }

    @Override
    public boolean isCollisionChecker() {
        return mCheckCollisions;
    }
    
    /** Enables or disables collision checking for this BV. */
    public void setCollisionCheck(boolean enable) {
        mCheckCollisions = enable;
    }

    /**
     * Tests wether or not a point is within the bounding volume.
     * @param x
     * @param y
     * @param z
     * @return True if point is within or touches bounding volume.
     */
    public abstract boolean isInVolume(float x, float y, float z);

    @Override
    public IntersectionDistance getRayDistance(float[] s, float[] v) {
        return null;
    }

    /**
     * Returns the distance from volume's center to its border in
     * the given direction.
     * @param direction Normalized direction vector.
     * @return Distance.
     */
    public abstract float getCenterBorderDistance(float[] direction);
    
    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {
        // invalidate since game object transformation may have 
        // since last frame
        centerWorldValid = false;
    }

    /**
     * Test whether or not this bounding volume intersects another one.
     * The collision point is simply the point where the center-to-center
     * line exits this bounding volume.
     * @param bv
     * @return Point of collision
     */
    protected float[] intersects(BaseSimpleBoundingVolume bv) {
        // get center points of both volumes in world space
        if (!centerWorldValid) {
            final float[] toWorld = getGameObj().getToWorld();
            MatrixAF.multiplyMV(mCenterWorld, 0, toWorld, 0, mCenter, 0);
            centerWorldValid = true;
        }
        final float[] otherCenterWorld = bv.getCenter();
        
        // distance between center of both volumes
        VectorAF.subtract(mV, mCenterWorld, otherCenterWorld);
        final float centerDistance = VectorAF.normalize(mV);
        
        // get center-border distances for both volumes
        final float otherBorderDistance = bv.getCenterBorderDistance(mV);
        mV[0] *= -1;
        mV[1] *= -1;
        mV[2] *= -1;
        final float thisBorderDistance = getCenterBorderDistance(mV);
        
     
        if (centerDistance < thisBorderDistance + otherBorderDistance) {
            // calculate collision point
            mCollisionPoint[0] = mCenterWorld[0] + thisBorderDistance * mV[0];
            mCollisionPoint[1] = mCenterWorld[1] + thisBorderDistance * mV[1];
            mCollisionPoint[2] = mCenterWorld[2] + thisBorderDistance * mV[2];
            mCollisionPoint[3] = 1;
            return mCollisionPoint;
        }
        
        return null;
    }
    
    public float[] getCenter() {
        if (!centerWorldValid) {
            final float[] toWorld = getGameObj().getToWorld();
            MatrixAF.multiplyMV(mCenterWorld, 0, toWorld, 0, mCenter, 0);
            centerWorldValid = true;
        }
        return mCenterWorld;
    }

}
