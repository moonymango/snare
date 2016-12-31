package com.moonymango.snare.game;

public interface IGameStateLogic {
    /** 
     * Called every frame as long as related game state(s) are
     * active. Change game state by returning the desired state. 
     * Returning null will keep actual game state.
     * 
     * @param logic
     * @param realTime
     * @param realDelta
     * @param virtualDelta
     * @return 
     */
    IGameState onUpdate(long realTime, float realDelta, float virtualDelta);
    
    /**
     * Called when related game state becomes the active state.
     * @param previous Previous game state, null for very first state.
     */
    void onActivate(IGameState previous);
    
    /**
     * Called when related game state is about to be replaced by
     * another game state.
     * active game state.
     * @param next Next game state
     */
    void onDeactivate(IGameState next);
    
    /**
     * Initialize the logic. It is up to the user to call this at a
     * convenient time (e.g. during startup). The engine itself 
     * won't invoke calls to this.
     */
    void onInit();
    
    /**
     * Shutdown logic. Intended for states that are surely known to
     * not become active again. It is up to the user to call this at a
     * convenient time (e.g. during startup). The engine itself 
     * won't invoke calls to this.
     */
    void onShutdown();
    
    /**
     * Tells logic what the next state is. Intended for simple transient states
     * that do not decide the next game state on their own. The engine itself 
     * won't invoke calls to this.
     * @param next
     */
    void setNextState(IGameState next);
}
