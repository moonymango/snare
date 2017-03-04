package com.moonymango.snare.physics;

import com.moonymango.snare.ui.scene3D.BaseCamera;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.MatrixAF;

/**
 * Bounding volume for squares (so technically not really a volume)
 * This is only intended for use with raycasts, not collision checking.
 * In the local space the square is in the x-z plane and axis aligned at the origin.
 */

public class SimpleSquareBoundingVolume extends BaseSimpleBoundingVolume
{
    private final IntersectionDistance mDistance = new IntersectionDistance();
    private final float[] mN = {0, 1, 0, 0};  // normal vector of the square
    private final float[] mTmp = new float[4];
    private float mMinX;
    private float mMaxX;
    private float mMinZ;
    private float mMaxZ;

    public SimpleSquareBoundingVolume()
    {
        super(false);  // no collision checking
    }

    @Override
    public void setDimensions(IBoundingVolumeProvider provider)
    {
        mMaxX = provider.getMaxX();
        mMinX = provider.getMinX();
        mMaxZ = provider.getMaxZ();
        mMinZ = provider.getMinZ();
    }

    @Override
    public boolean isInVolume(float x, float y, float z)
    {
       throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public float getCenterBorderDistance(float[] direction)
    {
        throw new UnsupportedOperationException("not implemented.");
    }

    @Override
    public boolean isInFrustrum(BaseCamera c)
    {
        throw new UnsupportedOperationException("function not implemented.");
    }

    @Override
    public float getSizeX()
    {
        return mMaxX-mMinX;
    }

    @Override
    public float getSizeY()
    {
        return 0;  // always 0
    }

    @Override
    public float getSizeZ()
    {
        return mMaxZ-mMinZ;
    }

    @Override
    public IntersectionDistance getRayDistance(float[] s, float[] v)
    {
        // transform ray to this object's local space
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mS, 0, fromWorld, 0, s, 0);
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, v, 0);
        return getRayDistance();
    }

    private IntersectionDistance getRayDistance()
    {
        // get intersection point between ray and x-z plane
        final float t = Geometry.planeIntersection(mN, mS, mV, mTmp);
        if (Float.isNaN(t))
        {
            return null;  // no intersection between ray and xz plane
        }

        // check if intersection point is contained in square (intersection point is returned in mTmp)
        boolean b = mTmp[0] > mMinX && mTmp[0] < mMaxX;
        b &= mTmp[2] > mMinZ && mTmp[2] < mMaxZ;

        if (b)
        {
            mDistance.MIN = t;
            mDistance.MAX = t;
            return mDistance;
        }

        return null;  // intersection point is not within square
    }
}
