package com.moonymango.snare.game;

/**
 * Convenience class that combines {@link IGameState} and {@link IGameStateLogic}.
 * Intended to be used when only a single game state is required.
 */
public abstract class SimpleGameState implements IGameState, IGameStateLogic {
    
    @Override
    public void onInit() {}
        
    @Override
    public void onShutdown() {}

    @Override
    public void setNextState(IGameState next) {}

    @Override
    public void onDeactivate(IGameState next) {
        // single state, never gets deactivated
    }

    @Override
    public IGameStateLogic getGameStateLogic() {
        return this;
    }

    @Override
    public String getName() {
        return SimpleGameState.class.getName();
    }

    @Override
    public boolean equals(IGameState state) {
        return state == this;
    }

}
