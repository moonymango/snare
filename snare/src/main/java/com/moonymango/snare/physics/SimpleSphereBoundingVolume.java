package com.moonymango.snare.physics;

import com.moonymango.snare.ui.scene3D.BaseCamera;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.VectorAF;

/**
 * Sphere bounding volume. Probably a little bit faster than {@link SimpleAABB}.
 */
public class SimpleSphereBoundingVolume extends BaseSimpleBoundingVolume {

    private float mRadius = 1;
    private final IntersectionDistance mDistance = new IntersectionDistance();
    
    protected SimpleSphereBoundingVolume(boolean checkCollisions) {
        super(checkCollisions);
    }
    
    @Override
    public VolumeType getType() {
        return VolumeType.SPHERE;
    }

    /**
     * Sets radius of the sphere.
     * @param radius
     */
    public void setDimensions(float radius) {
        mRadius = radius;
        mCenter[0] = 0;
        mCenter[1] = 0;
        mCenter[2] = 0;
        mCenter[3] = 1;
    }
    
    /**
     * Sets dimensions from provider data.
     * NOTE: The radius of the resulting sphere is the maximum
     * distance between the provider's min/max values.
     * Compared to an {@link SimpleAABB} created by the same provider, the resulting 
     * spere will not contain some space around the corners of the {@link SimpleAABB}.
     * @param provider
     */
    @Override
    public void setDimensions(IBoundingVolumeProvider provider) {
        final float rx = (provider.getMaxX() - provider.getMinX()) / 2;
        final float ry = (provider.getMaxY() - provider.getMinY()) / 2;
        final float rz = (provider.getMaxZ() - provider.getMinZ()) / 2;
        mCenter[0] = provider.getMinX() + rx;
        mCenter[1] = provider.getMinY() + ry;
        mCenter[2] = provider.getMinZ() + rz;
        mCenter[3] = 1;
        mRadius = Math.max(rx, Math.max(ry, rz));
    }

    @Override
    public boolean isInFrustrum(BaseCamera c) {
        // TODO prioD: visibility test
        return false;
    }

    @Override
    public boolean isInVolume(float x, float y, float z) {
        // transform point to to this object's local space
        mV[0] = x;
        mV[1] = y;
        mV[2] = z;
        mV[3] = 1;
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mS, 0, fromWorld, 0, mV, 0);
        
        // compare distance to center with radius
        VectorAF.subtract(mV, mCenter, mS);
        final float distance = VectorAF.mag(mV);
        return distance < mRadius;
    }

    @Override
    public IntersectionDistance getRayDistance(float[] s, float[] v) {
        // transform ray to to this object's local space
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mS, 0, fromWorld, 0, s, 0);
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, v, 0);
        
        // subtract sphere center from ray origin, so that we can use
        // a sphere located at origin for intersection testing
        mS[0] -= mCenter[0];
        mS[1] -= mCenter[1];
        mS[2] -= mCenter[2];
        mS[3] = 1;
        
        // quadratic equation for sphere x² + y² + z² = radius²
        final float a = mV[0]*mV[0] + mV[1]*mV[1] + mV[2]*mV[2];
        final float b = 2*(mS[0]*mV[0] + mS[1]*mV[1] + mS[2]*mV[2]);
        final float c = mS[0]*mS[0] + mS[1]*mS[1] + mS[2]*mS[2] - mRadius*mRadius;
        
        final float d = b*b - 4*a*c;
        if (d < 0) {
            return null;
        }
        final float rd = (float) Math.sqrt(d);
        
        mDistance.MIN = (-b-rd)/(2*a);
        mDistance.MAX = (-b+rd)/(2*a);
        return mDistance;
    }

    @Override
    public float getCenterBorderDistance(float[] direction) {
        // transform direction to local space
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, direction, 0);
        
        final float mag = VectorAF.mag(mV);
        return mRadius/mag;
    }

    @Override
    public float getSizeX() {
        return 2*mRadius;
    }

    @Override
    public float getSizeY() {
        return 2*mRadius;
    }

    @Override
    public float getSizeZ() {
        return 2*mRadius;
    }

}
