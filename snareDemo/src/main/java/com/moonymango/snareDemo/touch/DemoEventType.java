package com.moonymango.snareDemo.touch;

import com.moonymango.snare.events.EventManager.IEventType;

public enum DemoEventType implements IEventType {

    DEMO_OBJ_CATCHED;
    
    public String getName() {
        return this.name();
    }
}
