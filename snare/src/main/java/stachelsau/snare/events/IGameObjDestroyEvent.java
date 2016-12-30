package stachelsau.snare.events;

import stachelsau.snare.game.GameObj;


/**
 * Event: game obj removed from logic.
 */
public interface IGameObjDestroyEvent extends IEvent {

    public static final SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_DESTROY;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj); 
    
}
