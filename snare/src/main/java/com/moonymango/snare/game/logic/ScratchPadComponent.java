package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGame;

/**
 * Component that allows decoration of {@link GameObj} with arbitrary data.
 */
public class ScratchPadComponent extends BaseComponent {

    private final Object[] mObjs;
    
    public ScratchPadComponent(IGame game, int numObjs)
    {
        super(game, ComponentType.SCRATCH_PAD);
        mObjs = new Object[numObjs];
    }

    public Object getObject(int index) {
        return mObjs[index];
    }
    
    public void setData(int index, Object obj) {
        mObjs[index] = obj;
    }

    @Override
    public void reset() {
        // clear all references
        for (int i = mObjs.length-1; i >= 0; i--) {
            mObjs[i] = null;
        }
    }
    
    
}
