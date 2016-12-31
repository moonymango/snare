package com.moonymango.snare.util;

public abstract class CacheItem<C extends Cache<C,D,I>, D extends CacheItemDescriptor<C,D,I>, I extends CacheItem<C,D,I>> {

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    protected final D mDescriptor;
    long mRefCnt = 0;

    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public CacheItem(D descriptor) {
        mDescriptor = descriptor;
    }
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------

    /**
     * Called when item was newly created and added to the cache.
     */
    public abstract void onAddedToCache();
    
    /**
     * Called when reference to the item was requested from the cache. 
     */
    public abstract void onRefCntIncr();
    
    /**
     * Called when a reference to the item was released.
     */
    public abstract void onRefCntDecr();
    
    /**
     * Called when the cache wants to remove the item, probably because it
     * has no references left.
     * NOTE: when false is returned, the cache will NEVER remove this item,
     * so be sure you know what you are doing!
     *  @return false to prevent cache from removing the item, true otherwise
     */
    public abstract boolean onRemoveFromCachePending();
    
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    @Override
    public String toString() {
        return String.format("cache item: %s", mDescriptor.toString());
    }
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
