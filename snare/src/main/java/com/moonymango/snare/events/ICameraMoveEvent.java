package com.moonymango.snare.events;

public interface ICameraMoveEvent extends IEvent {

    SystemEventType EVENT_TYPE = SystemEventType.UI_CAMERA_MOVE;
    
    float[] getCamPosition();
    void setCamPosition(float x, float y, float z);
    
}
