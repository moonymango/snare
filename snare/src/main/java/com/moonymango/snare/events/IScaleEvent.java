package com.moonymango.snare.events;


public interface IScaleEvent extends IEvent {

    SystemEventType EVENT_TYPE = SystemEventType.UI_SCALE;
    
    float getScaleFactor();
    boolean isNewScaleGesture();
    void setScaleData(float factor, boolean newGesture);
}
