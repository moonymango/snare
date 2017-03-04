package com.moonymango.snare.physics;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;
import com.moonymango.snare.ui.scene3D.BaseCamera;

public abstract class BaseBoundingVolume implements IComponent {
    
    private GameObj mGameObj;
           
       
    public abstract VolumeType getType();
    
    /**
     * Sets dimensions of bounding volume.
     * @param provider
     */
    public abstract void setDimensions(IBoundingVolumeProvider provider);
        
    /**
     * Indicates if this bounding volume wants to be checked for
     * collisions with others.
     * @return
     */
    public abstract boolean isCollisionChecker();
    
    /**
     * Enables or disables collision checking.
     * @param enable
     */
    public abstract void setCollisionCheck(boolean enable);
    
    /**
     * Returns distance of ray origin to intersection point with this
     * bounding volume. 
     * @param s Origin of ray.
     * @param v Direction of ray. 
     * @return Distances in case of intersection, null otherwise. 
     */
    public abstract IntersectionDistance getRayDistance(float[] s, float[] v);

    public void onTraverse(Object userData) {}

    public ComponentType getComponentType() {
        return ComponentType.BOUNDING_VOLUME;
    }

    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}

    public void onInit() {}
    public void onShutdown() {}
    public void reset() {}

    public GameObj getGameObj() {return mGameObj;}
    public void setGameObj(GameObj obj) {mGameObj = obj;}
    
    /**
     * Tests if bounding volume is within the frustrum of a camera.
     * @param c
     * @return
     */
    public abstract boolean isInFrustrum(BaseCamera c);
    /**
     * Returns center point of bounding volume in world coordinates. 
     */
    public abstract float[] getCenter();
    /** Dimension along x axis (local space) */
    public abstract float getSizeX();
    /** Dimension along y axis (local space) */
    public abstract float getSizeY();
    /** Dimension along z axis (local space) */
    public abstract float getSizeZ();
    
    /**
     * Set of distance values as result of an intersection of a
     * ray with a bounding volume (BV). MIN represents the 
     * distance from the ray's origin to the entry point, MAX to the exit point
     * at the BV. Actual point coordinates can be calculates as follows:
     *  
     * </br></br>
     * p = ray_origin + d * ray_direction 
     * (where d is either MIN or MAX given by this {@link IntersectionDistance})
     * </br></br>
     * 
     * The values of MIN and MAX can be interpreted as follows:
     * <ul>
     * <li>both MIN and MAX positive: BV lies towards ray's direction and
     *      origin is outside BV</li>
     * <li>MIN negative and MAX positive: origin lies inside BV, entry point given
     *      by MIN lies "behind" origin</li>
     * <li>both MIN and MAX negative: BV lies towards opposite ray direction 
     *      ("behind" origin) and origin is outside of BV</li>      
     */
    public static class IntersectionDistance {
        /** 
         * Minimal distance, this is the point where 
         * ray enters bounding volume 
         * */
        public float MIN;
        /** 
         * Maximal distance, this is the point where 
         * ray exits bounding volume 
         * */
        public float MAX;
    }
    
    public enum VolumeType {
        BOX,
        SPHERE,
        CYLINDER,
        SQUARE,
    }
    
}
