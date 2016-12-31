package com.moonymango.snare.events;

import android.view.MotionEvent;

public interface IFlingEvent extends IEvent {

    SystemEventType EVENT_TYPE = SystemEventType.UI_FLING;
    
    /** X coordinate as delivered by {@link MotionEvent} */
    int getTouchX();
    /** Inverted y coordinate (screenHeight - {@link MotionEvent}.getY()) */
    int getTouchY();
    float getVelocityX();
    float getVelocityY(); 
    void setFlingData(int x, int y, float velocityX, float velocityY);
}
