package com.moonymango.snare.game;

import com.moonymango.snare.game.logic.NullGameStateLogic;

public interface IGameState {

    /**
     * Gets the logic for this game state. Returning null
     * is not allowed, return {@link NullGameStateLogic} instead.
     * @return
     */
    IGameStateLogic getGameStateLogic();
    String getName();
    boolean equals(IGameState state);
    
}
