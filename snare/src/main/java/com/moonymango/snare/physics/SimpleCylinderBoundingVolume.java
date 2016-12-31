package com.moonymango.snare.physics;

import com.moonymango.snare.ui.scene3D.BaseCamera;
import com.moonymango.snare.util.MatrixAF;

public class SimpleCylinderBoundingVolume extends BaseSimpleBoundingVolume {

    private AxisOrientation mOrientation = AxisOrientation.Y;
    private float mRadius = 1;
    private float mHeightAxisMin = -0.5f;
    private float mHeightAxisMax = 0.5f;
    
    private final IntersectionDistance mDistance = new IntersectionDistance();
    
    protected SimpleCylinderBoundingVolume(boolean checkCollisions) {
        super(checkCollisions);
    }
    
    @Override
    public VolumeType getType() {
        return VolumeType.SPHERE;
    }

    /**
     * Sets axis which is height of the cylinder.
     * @param o
     */
    public void setAxisOrientation(AxisOrientation o) {
        mOrientation = o;
    }
    
    public AxisOrientation getAxisOrientation() {
        return mOrientation;
    }
    
    /**
     * Sets radius and length of the cylinder.
     * @param radius
     */
    public void setDimensions(float radius, float height) {
        mRadius = radius;
        mHeightAxisMax = height/2;
        mHeightAxisMin = -mHeightAxisMax;
        mCenter[0] = 0;
        mCenter[1] = 0;
        mCenter[2] = 0;
        mCenter[3] = 1;
    }
    
    /**
     * Sets dimensions from provider data based on actual axis orientation.
     * Radius is the maximum value of both non-height axes.
     * 
     * @param provider
     */
    @Override
    public void setDimensions(IBoundingVolumeProvider provider) {
        final float hdx = (provider.getMaxX() - provider.getMinX())/2;
        final float hdy = (provider.getMaxY() - provider.getMinY())/2;
        final float hdz = (provider.getMaxZ() - provider.getMinZ())/2;
        
        switch(mOrientation) {
        case X:
            mRadius = Math.max(hdy, hdz);
            mHeightAxisMin = provider.getMinX();
            mHeightAxisMax = provider.getMaxX();
            break;
            
        case Y:
            mRadius = Math.max(hdx, hdz);
            mHeightAxisMin = provider.getMinY();
            mHeightAxisMax = provider.getMaxY();
            break;
            
        case Z:
            mRadius = Math.max(hdx, hdy);
            mHeightAxisMin = provider.getMinZ();
            mHeightAxisMax = provider.getMaxZ();
            break;
        }
        mCenter[0] = provider.getMinX() + hdx;
        mCenter[1] = provider.getMinY() + hdy;
        mCenter[2] = provider.getMinZ() + hdz;
        mCenter[3] = 1;
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
         
        switch (mOrientation) {
        case X:
            if (mS[0] < mHeightAxisMin || mS[0] > mHeightAxisMax 
                    || mS[1]*mS[1] + mS[2]*mS[2] > mRadius*mRadius) {
                return false;
            }
            break;
            
        case Y:
            if (mS[1] < mHeightAxisMin || mS[1] > mHeightAxisMax
                    || mS[0]*mS[0] + mS[2]*mS[2] > mRadius*mRadius) {
                return false;
            }
            break;
            
        case Z:
            if (mS[2] < mHeightAxisMin || mS[2] > mHeightAxisMax
                    || mS[0]*mS[0] + mS[1]*mS[1] > mRadius*mRadius) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public IntersectionDistance getRayDistance(float[] s, float[] v) {
        
        // transform ray to to this object's local space
        final float[] fromWorld = getGameObj().getFromWorld();
        MatrixAF.multiplyMV(mS, 0, fromWorld, 0, s, 0);
        MatrixAF.multiplyMV(mV, 0, fromWorld, 0, v, 0);
        
        // subtract sphere center from ray origin, so that we can use
        // a cylinder located at origin for intersection testing
        //mS[0] -= mCenter[0];
        //mS[1] -= mCenter[1];
        //mS[2] -= mCenter[2];
        //mS[3] = 1;
        
        // quadratic equation for infinite cylinder surface x² + y² = radius²
        // (in case axis orientation is z)
        float v0=0, v1=0, s0=0, s1=0, hv=0, hs=0;
        switch (mOrientation) {
        case X:
            // oriented along x, use y and z for equation
            v0 = mV[1];
            v1 = mV[2];
            s0 = mS[1];
            s1 = mS[2];
            // x 
            hv = mV[0];
            hs = mS[0];
            break;
            
        case Y:
            // oriented along y, use x and z for equation
            v0 = mV[0];
            v1 = mV[2];
            s0 = mS[0];
            s1 = mS[2];
            // y 
            hv = mV[1];
            hs = mS[1];
            break;
            
        case Z:
            // oriented along z, use x and y for equation
            v0 = mV[0];
            v1 = mV[1];
            s0 = mS[0];
            s1 = mS[1];
            // z 
            hv = mV[2];
            hs = mS[2];
            break;
            
        }
        
        final float a = v0*v0 + v1*v1;
        final float b = 2*(s0*v0 + s1*v1);
        final float c = s0*s0 + s1*s1 - mRadius*mRadius;
        
        final float d = b*b - 4*a*c;
        if (d < 0) {
            return null;
        }
        
        // we have an intersection with infinite cylinder, test points
        // against height axis
        final float rd = (float) Math.sqrt(d);
        mDistance.MIN = (-b-rd)/(2*a);
        mDistance.MAX = (-b+rd)/(2*a);
        
        float h = hs + mDistance.MIN*hv;
        if (h <= mHeightAxisMax && h >= mHeightAxisMin) {
            return mDistance;
        }
        h = hs + mDistance.MAX*hv;
        if (h <= mHeightAxisMax && h >= mHeightAxisMin) {
            return mDistance;
        }
        
        return null;
    }

    @Override
    public float getCenterBorderDistance(float[] direction) {
        throw new UnsupportedOperationException("Not implemented. Collision" +
        		" checking not supported.");
    }

    public enum AxisOrientation {
        X, Y, Z
    }

    @Override
    public float getSizeX() {
        if (mOrientation == AxisOrientation.X)
            return mHeightAxisMax - mHeightAxisMin;
        return 2*mRadius;
    }

    @Override
    public float getSizeY() {
        if (mOrientation == AxisOrientation.Y)
            return mHeightAxisMax - mHeightAxisMin;
        return 2*mRadius;
    }

    @Override
    public float getSizeZ() {
        if (mOrientation == AxisOrientation.Z)
            return (mHeightAxisMax - mHeightAxisMin);
        return 2*mRadius;
    }

}
