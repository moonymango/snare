package com.moonymango.snareDemo.touch;

import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.game.GameObj;

public interface IDemoObjCatchedEvent extends IEvent {
    
    public static final DemoEventType EVENT_TYPE = DemoEventType.DEMO_OBJ_CATCHED;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj);

}
