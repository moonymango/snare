package com.moonymango.snare.events;


public interface IKeyEvent extends IEvent {

    public static final SystemEventType EVENT_TYPE = SystemEventType.UI_KEY;
    int getAction();
    int getKeyCode();
    void setKeyData(int keyCode, int action);
    
}
