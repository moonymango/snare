package stachelsau.snare.game.logic;

import stachelsau.snare.game.IGameState;
import stachelsau.snare.game.IGameStateLogic;

/**
 * Empty logic that has no effect. Intended as place holder
 * because every game state has to deliver a game state logic.
 */
public class NullGameStateLogic implements IGameStateLogic {

    private IGameState mNext;
    
    @Override
    public IGameState onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return mNext;
    }

    @Override
    public void onActivate(IGameState previous) {

    }

    @Override
    public void onDeactivate(IGameState next) {

    }

    @Override
    public void onInit() {
                
    }

    @Override
    public void onShutdown() {
                
    }

    @Override
    public void setNextState(IGameState next) {
        mNext = next;
    }

}
