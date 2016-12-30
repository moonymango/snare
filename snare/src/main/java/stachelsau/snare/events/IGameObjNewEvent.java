package stachelsau.snare.events;

import stachelsau.snare.game.GameObj;


/**
 * Event: new game object added to game logic
 */
public interface IGameObjNewEvent extends IEvent {

    public static final SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_NEW;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj);
}
