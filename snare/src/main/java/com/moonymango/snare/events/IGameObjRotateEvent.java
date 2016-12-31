package com.moonymango.snare.events;


public interface IGameObjRotateEvent extends IEvent {
    SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_ROTATE;
    
    int getGameObjID();
    void setGameObjData(int id);
}
