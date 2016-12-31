package com.moonymango.snare.events;

import com.moonymango.snare.game.IGameState;

/**
 * Notifies about change of game state.
 */
public interface IGameStateChangedEvent extends IEvent {
    public static final SystemEventType EVENT_TYPE = 
            SystemEventType.GAME_STATE_CHANGED;
    
    /** Returns the new state. */
    IGameState getNewState();
    /** Returns the previous state. */
    IGameState getPrevState();
    void setGameStateData(IGameState newState, IGameState prevState);
}
