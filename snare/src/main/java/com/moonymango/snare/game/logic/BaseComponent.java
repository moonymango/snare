package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;

public abstract class BaseComponent implements IComponent {

    private GameObj mGameObj;
    private final ComponentType mType;
    
    protected BaseComponent(ComponentType type) {
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
