package stachelsau.snare.demo.touch;

import stachelsau.snare.events.IEvent;
import stachelsau.snare.game.GameObj;

public interface IDemoObjCatchedEvent extends IEvent {
    
    public static final DemoEventType EVENT_TYPE = DemoEventType.DEMO_OBJ_CATCHED;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj);

}
