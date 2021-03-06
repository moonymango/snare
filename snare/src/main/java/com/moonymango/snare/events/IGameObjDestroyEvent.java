package com.moonymango.snare.events;

import com.moonymango.snare.game.GameObj;


/**
 * Event: game obj removed from logic.
 */
public interface IGameObjDestroyEvent extends IEvent {

    SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_DESTROY;
    
    GameObj getGameObj();
    void setGameObj(GameObj obj); 
    
}
