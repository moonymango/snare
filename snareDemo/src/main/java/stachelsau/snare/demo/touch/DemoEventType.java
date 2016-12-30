package stachelsau.snare.demo.touch;

import stachelsau.snare.events.EventManager.IEventType;

public enum DemoEventType implements IEventType {

    DEMO_OBJ_CATCHED;
    
    public String getName() {
        return this.name();
    }
}
