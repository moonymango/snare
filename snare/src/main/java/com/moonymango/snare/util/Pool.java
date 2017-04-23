package com.moonymango.snare.util;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.util.Logger.LogSource;

import java.util.ArrayList;


public abstract class Pool<T extends PoolItem> extends BaseSnareClass {

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    private static final int DEFAULT_CAPACITY_INCREMENT = 20;
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    private final ArrayList<T> mItems = new ArrayList<>();
    private final int mCapacityIncrement;
    
    private int mObtained = 0;
    private int mRecycled = 0;
    
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public Pool(IGame game)
    {
        this(game, DEFAULT_CAPACITY_INCREMENT);
    }
    
    
    public Pool(IGame game, int capacityIncrement)
    {
        super(game);

        if (capacityIncrement <= 0) {
            mCapacityIncrement = DEFAULT_CAPACITY_INCREMENT;
        } else {
            mCapacityIncrement = capacityIncrement;
        }
        
        for (int i = 0; i < mCapacityIncrement; i++) {
            T item = allocatePoolItem();
            if (item == null)
                throw new IllegalStateException("allocatePoolItem() returned null.");
            mItems.add(item);
        }
    }
    
       
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public T obtain() {
        if (mItems.isEmpty()) {
            for (int i = 0; i < mCapacityIncrement; i++) {
                T item = allocatePoolItem();
                if (item == null)
                    throw new IllegalStateException("allocatePoolItem() returned null.");
                mItems.add(item);
            }
        }
            
        T item = mItems.remove(mItems.size()-1);
        item.mPool = this;
        item.mIsRecycled = false;
        mObtained++;
        return item;
    }
    
    @SuppressWarnings("unchecked")
    public void recycle(PoolItem item) {
        if (item == null || item.mPool != this || item.mIsRecycled) return;
        item.mIsRecycled = true;
        mItems.add((T) item);
        mRecycled++;
        if (mRecycled > mObtained) {
            Logger.w(LogSource.UTILS, "Pool: " + mRecycled + " items recycled, but only " + mObtained + " obtained, stack size " + mItems.size());
            mObtained = mRecycled; 
        }
    }
    
    public int getCapacity() {
        return mItems.size();
    }
    
    protected abstract T allocatePoolItem();
    
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
    
}
