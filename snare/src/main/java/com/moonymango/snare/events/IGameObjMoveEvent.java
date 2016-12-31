package com.moonymango.snare.events;


/**
 * Event: game obj changed position
 */
public interface IGameObjMoveEvent extends IEvent {
    
    SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_MOVE;
    
    int getGameObjID();
    float[] getPosition();
    void setGameObjData(int objID, float x, float y, float z);
    
}
