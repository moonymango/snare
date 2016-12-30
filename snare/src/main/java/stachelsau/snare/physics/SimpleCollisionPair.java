package stachelsau.snare.physics;

import stachelsau.snare.util.PoolItem;

/**
 * Holds a pair of bounding volumes intended for collision tests.
 * 
 * Note: After init() the collision test is only performed once and the results
 *       are cached. After that single test only the cached results are
 *       returned, which may not be valid in case the game objects
 *       have moved in the meantime. You need to init() again to
 *       perform a new test. 
 */
public class SimpleCollisionPair extends PoolItem implements ICollisionPair {
    
    /**
     * Computes hash of a pair based on object ids.
     * @param bvA bounding volume
     * @param bvB bounding volume
     * @return Hash value independent of object order.
     */
    public static int calcHash(BaseBoundingVolume bvA, BaseBoundingVolume bvB) {
        final int idA = bvA.getGameObj().getID();
        final int idB = bvB.getGameObj().getID();
        final int small = idA < idB ? idA : idB;
        final int big = idA < idB ? idB : idA;
        
        final int prime = 31;
        int result = 1;
        result = prime * result + small;
        result = prime * result + big;
        return result;
    }
    
    private BaseSimpleBoundingVolume mBvA;
    private BaseSimpleBoundingVolume mBvB;
    private int mHash; 
    private boolean mAlreadyTested;
    private boolean mIsCollision;
    private float[] mCollisionPoint = {0, 0, 0, 1};
    
    public void init(BaseSimpleBoundingVolume bvA, BaseSimpleBoundingVolume bvB) {
        mBvA = bvA;
        mBvB = bvB;
        mHash = calcHash(bvA, bvB);
        mAlreadyTested = false;
    }

    /* (non-Javadoc)
     * @see stachelsau.snare.physics.ICollisionPair#getObjIdA()
     */
    @Override
    public int getObjIdA() {
        return mBvA.getGameObj().getID();
    }
    
    /* (non-Javadoc)
     * @see stachelsau.snare.physics.ICollisionPair#getObjIdB()
     */
    @Override
    public int getObjIdB() {
        return mBvB.getGameObj().getID();
    }
    
    public boolean isCollision() {
        if (!mAlreadyTested) {
            testCollision();
        }
        return mIsCollision;
    }
    
    /* (non-Javadoc)
     * @see stachelsau.snare.physics.ICollisionPair#getCollisionPoint()
     */
    @Override
    public float[] getCollisionPoint() {
        if (!mAlreadyTested) {
            testCollision();
        }
        return mCollisionPoint;
    }
    
    private void testCollision() {
        final float[] p = mBvA.intersects(mBvB);
        if (p != null) {
            mIsCollision = true;
            mCollisionPoint[0] = p[0];
            mCollisionPoint[1] = p[1];
            mCollisionPoint[2] = p[2];
        } else {
            mIsCollision = false;
        }
        mAlreadyTested = true;
    }
    
    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleCollisionPair other = (SimpleCollisionPair) obj;
        if (mHash != other.mHash)
            return false;
        return true;
    }
    
}
