package com.moonymango.snare.events;

import com.moonymango.snare.game.GameObj;


/**
 * Event: new game object added to game logic
 */
public interface IGameObjNewEvent extends IEvent {

    SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_NEW;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj);
}
