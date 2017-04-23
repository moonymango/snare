package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;
import com.moonymango.snare.game.IGame;

public abstract class BaseComponent extends BaseSnareClass implements IComponent {

    private GameObj mGameObj;
    private final ComponentType mType;
    
    protected BaseComponent(IGame game, ComponentType type)
    {
        super(game);
        mType = type;
    }
    
    public ComponentType getComponentType() {
        return mType;
    }

    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}
    public void onTraverse(Object userData) {}
    public void onInit() {}
    public void onShutdown() {}
    public void reset() {}

    public GameObj getGameObj() {return mGameObj;}
    public void setGameObj(GameObj obj) {mGameObj = obj;}
    
}
