package com.moonymango.snare.events;

import com.moonymango.snare.ui.TouchAction;
import android.view.MotionEvent;

/**
 * Event: user touched screen.
 */
public interface ITouchEvent extends IEvent {
    
    public static final SystemEventType EVENT_TYPE = SystemEventType.UI_TOUCH;
    
    TouchAction getTouchAction();
    /** X position of touch as delivered by {@link MotionEvent}. */
    int getTouchX();
    /** 
     * Inverted Y position of touch (screenHeight - {@link MotionEvent}.getY()),
     * so bottom of screen has y coordinate of 0.
     */
    int getTouchY();
    void setTouchData(TouchAction action, int x, int y);
    
}
