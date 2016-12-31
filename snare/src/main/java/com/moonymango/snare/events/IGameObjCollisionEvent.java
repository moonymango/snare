package com.moonymango.snare.events;


public interface IGameObjCollisionEvent extends IEvent {

public static final SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_COLLISION;
    /** Return id of one obj in this collision */
    int getGameObjID();
    /** Return id of other obj in this collision */
    int getOtherGameObjID();
    
    float[] getCollisionPoint();
    /**
     * Set collision data.
     * @param objID
     * @param otherObjID
     * @param x Collision point x
     * @param y Collision point y
     * @param z Collision point z
     */
    void setGameObjData(int objID, int otherObjID, float x, float y, float z);
    
}
