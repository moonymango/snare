package com.moonymango.snare.physics;

import com.moonymango.snare.ui.scene3D.BaseCamera;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.VectorAF;


/** axis aligned bounding box **/
public class SimpleAABB extends BaseSimpleBoundingVolume {
    
    private final float[] mMin = new float[4];
    private final float[] mMax = new float[4];
    /** distance from center to any corner point of the box */
    protected float mEffectiveRadius;
    private final IntersectionDistance mDistance = new IntersectionDistance();
    

    protected SimpleAABB(boolean checkCollisions) {
        super(checkCollisions);
    }
    
    @Override
    public VolumeType getType() {
        return VolumeType.BOX;
    }

    /**
     * Sets minimum and maximum points of the AABB
     * @param min
     * @param max
     */
    public void setDimensions(float[] min, float[] max) {
        for (int i = 0; i < 3; i++) {
            mMin[i] = min[i];
            mMax[i] = max[i];
        }
        if (mMin[0] >= mMax[0] || mMin[1] >= mMax[1] || mMin[2] >= mMax[2]) {
            throw new IllegalArgumentException("Min. is not less than max.");
        }
        
        mMin[3] = 1;
        mMax[3] = 1;
        calcCenter();
    }
    
    @Override
    public void setDimensions(IBoundingVolumeProvider provider) {
        // TODO get dimensions from mesh component in component's onInit()
        // instead of this method
        mMin[0] = provider.getMinX();
        mMin[1] = provider.getMinY();
        mMin[2] = provider.getMinZ();
        mMax[0] = provider.getMaxX();
        mMax[1] = provider.getMaxY();
        mMax[2] = provider.getMaxZ();
        mMin[3] = 1;
        mMax[3] = 1;
        if (mMin[0] >= mMax[0] || mMin[1] >= mMax[1] || mMin[2] >= mMax[2]) {
            throw new IllegalArgumentException("Min. is not less than max.");
        }
        calcCenter();
    }
    
    private void calcCenter() {
        // get vector from min to center for effective radius 
        mCenter[0] = (mMax[0] - mMin[0]) / 2;
        mCenter[1] = (mMax[1] - mMin[1]) / 2;
        mCenter[2] = (mMax[2] - mMin[2]) / 2;
        mCenter[3] = 0;
        mEffectiveRadius = VectorAF.mag(mCenter);
        
        // set actual center point
        mCenter[0] += mMin[0];
        mCenter[1] += mMin[1];
        mCenter[2] += mMin[2];
        mCenter[3] = 1;
    }

    @Override
    public boolean isInFrustrum(BaseCamera c) {
        // TODO prioD: visibility test
        return true;
    }

    @Override
    public IntersectionDistance getRayDistance(float[] s, float[] v) {
        // transform ray to to this object's local space
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mS, 0, fromWorld, 0, s, 0);
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, v, 0);
        return getRayDistance();
    }

    /**
     * Check for intersection (Smits' algorithm). This expects
     * the ray's origin was previously stored in mS and 
     * direction in mV.
     * @return
     */
    private IntersectionDistance getRayDistance() {
        // 
        float tmin, tmax, tymin, tymax, tzmin, tzmax; 
        
        if (mV[0] >= 0) {
            tmin = (mMin[0] - mS[0]) / mV[0];
            tmax = (mMax[0] - mS[0]) / mV[0];
        } else {
            tmax = (mMin[0] - mS[0]) / mV[0];
            tmin = (mMax[0] - mS[0]) / mV[0];
        }
        
        if (mV[1] >= 0) {
            tymin = (mMin[1] - mS[1]) / mV[1];
            tymax = (mMax[1] - mS[1]) / mV[1];
        } else {
            tymax = (mMin[1] - mS[1]) / mV[1];
            tymin = (mMax[1] - mS[1]) / mV[1];
        }
        
        if (tmin > tymax || tymin > tmax) {
            return null;
        }
        if (tymin > tmin) {
            tmin = tymin;
        }
        if (tymax < tmax) {
            tmax = tymax;
        }
        
        if (mV[2] >= 0) {
            tzmin = (mMin[2] - mS[2]) / mV[2];
            tzmax = (mMax[2] - mS[2]) / mV[2];
        } else {
            tzmax = (mMin[2] - mS[2]) / mV[2];
            tzmin = (mMax[2] - mS[2]) / mV[2];
        }
        
        if (tmin > tzmax || tzmin > tmax) {
            return null;
        }
        if (tzmin > tmin) {
            tmin = tzmin;
        }
        if (tzmax < tmax) {
            tmax = tzmax;
        }
        
        mDistance.MIN = tmin;
        mDistance.MAX = tmax;
        return mDistance;
    }
    
    @Override
    public float getCenterBorderDistance(float[] direction) {
        // use ray from center towards direction
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, direction, 0);
        for (int i = 0; i < 4; i++) {
            mS[i] = mCenter[i];
        }
        
        final IntersectionDistance d = getRayDistance();
        return Math.abs(d.MIN);
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
        
        if (mS[0] > mMax[0] || mS[0] < mMin[0]) {
            return false;
        }
        if (mS[1] > mMax[1] || mS[1] < mMin[1]) {
            return false;
        }
        return !(mS[2] > mMax[2] || mS[2] < mMin[2]);

    }

    @Override
    public float getSizeX() {
        return mMax[0] - mMin[0];
    }

    @Override
    public float getSizeY() {
        return mMax[1] - mMin[1];
    }

    @Override
    public float getSizeZ() {
        return mMax[2] - mMin[2];
    }

}
