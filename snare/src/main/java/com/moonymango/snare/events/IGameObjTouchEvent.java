package com.moonymango.snare.events;

import com.moonymango.snare.ui.TouchAction;

/**
 * Event: game object was touched on screen.
 */
public interface IGameObjTouchEvent extends IEvent {
    
    SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_TOUCH;
    
    /** Screen coordinates: x */
    int getTouchX();
    /** Screen coordinates: y */
    int getTouchY();
    int getGameObjID();
    /** 3D coordinates of touch point */
    float[] getTouchPoint();
    TouchAction getTouchAction();
    void setGameObjData(int objID, float[] point, TouchAction action, 
            int x, int y);

}
