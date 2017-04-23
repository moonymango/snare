package com.moonymango.snare.util;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;

public abstract class PoolItem extends BaseSnareClass
{
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    Pool<? extends PoolItem> mPool;
    boolean mIsRecycled = true;
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    public PoolItem(IGame game)
    {
        super(game);
    }

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public final boolean isRecycled() {
        return mIsRecycled;
    }
    
    public void recycle() {
        if (mPool != null) {
            mPool.recycle(this);
        }
    }
    
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
