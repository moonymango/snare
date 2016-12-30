package stachelsau.snare.physics;

import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.game.GameObj.GameObjLayer;

public interface IPhysics extends IEventListener {
    
    void onInit();
    void tick(long realTime, float realDelta, float virtualDelta);
    
    /**
     * Performs ray cast.
     * @param layerMask Object layers to test.
     * @param s Ray origin.
     * @param v Ray direction.
     * @return Raycast object containing a list of objects hit by ray.
     */
    Raycast doRaycast(float[] s, float[] v, GameObjLayer layerMask);

    /**
     * Creates a new bounding volume. Note: the volume will not immediately be part of
     * of the physics world. Only when its game object is added to the game logic, the
     * bounding volume will be effective.
     * @param type
     * @return
     */
    BaseBoundingVolume createBoundingVolume(BaseBoundingVolume.VolumeType type);
    
    /**
     * Enables collision checking on global level.
     * @param enable
     */
    void enableCollisionChecking(boolean enable);
}
