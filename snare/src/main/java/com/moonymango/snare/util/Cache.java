package com.moonymango.snare.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.moonymango.snare.proc.AsyncProc;

/**
 * TODO prioC: make this thread-safe, so that it can be used within 
 * {@link AsyncProc}.
 *
 * @param <C>
 * @param <D>
 * @param <I>
 */
public abstract class Cache<C extends Cache<C, D, I>, D extends CacheItemDescriptor<C, D, I>, I extends CacheItem<C, D, I>> {

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------  
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    protected Map<D, I> mCache = new HashMap<D, I>();
    protected List<I> mRecentlyUsed = new ArrayList<I>();
    private int mHandlesCnt; 
    private final CleanUpPolicy mCleanUpPolicy;
    
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public Cache(CleanUpPolicy cleanUpPolicy) {
        mCleanUpPolicy = cleanUpPolicy;
    }

    
    // ---------------------------------------------------------
    // methods
    // --------------------------------------------------------- 
    protected void releaseHandle(I item) {
        if (item == null) return;
        item.mRefCnt--;
        item.onRefCntDecr();
        if (item.mRefCnt <= 0 
                && mCleanUpPolicy == CleanUpPolicy.IMMEDIATELY
                && item.onRemoveFromCachePending()) {
            dropItem(item);
        }
        mHandlesCnt--;
    }
    
    @SuppressWarnings("unchecked")
    protected I getHandle(CacheItemDescriptor<C,D,I> descriptor) {
        D descr = (D) descriptor;
        I item = mCache.get(descr);
        if (item == null) {
            if (!allowItemCreation(descr)) {
                throw new IllegalStateException("Not allowed to create new item: " + descr.toString());
            }
            item = descr.createCacheItem();
            if (item == null) { 
                throw new NullPointerException("Unable to create item handle: " + descr.mName);
            }
            mCache.put(descr, item);
            mRecentlyUsed.add(item);
            onItemCreated(item);
            item.onAddedToCache();
        } else {
            if (!item.mDescriptor.mName.equals(descr.mName)) {
                throw new IllegalStateException("cache item descriptor hashCode collision: " 
                        + descr.mName + ", " 
                        + item.mDescriptor.mName);
            }
            // getting an item makes it "recently used", so put
            // it on top of the list
            mRecentlyUsed.remove(item);
            mRecentlyUsed.add(item);
        } 
        
        item.mRefCnt++;
        item.onRefCntIncr();
        mHandlesCnt++;
        return item;
    }
    
    public int getHandlesCnt() {
        return mHandlesCnt;
    }
    
    public int getItemCnt() {
        return mCache.size();
    }
    
    /**
     * Removes the least recently used item without handles 
     * from the cache. Return value false can mean two things:
     * 1. empty cache,
     * 2. all items in the cache have active handles left.
     * In both cases no item can be removed, so you need to release
     * some handles first, to make this function succeed.
     * @return true, if an item has been removed, false otherwise
     */
    protected boolean freeLeastRecentlyUsed() {
        int size = mRecentlyUsed.size();
        for (int i = 0; i < size; i++) {
            I item = mRecentlyUsed.get(i);
            if (item.mRefCnt <= 0 && item.onRemoveFromCachePending()) {
                dropItem(item);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Override to specify if creation of new item is allowed. Use
     * freeLeastRecentlyUsed() in here to remove items if there is
     * no space for new items.
     */
    protected abstract boolean allowItemCreation(D descr);
    
    /**
     * Called when new item was created.
     * @param item
     */
    protected void onItemCreated(I item) {}
    /**
     * Called when an item was removed.
     * @param item
     */
    protected void onItemRemoved(I item) {}
    
    /**
     * Removes an item from the cache.
     * Note: The item will be removed without any check for references.
     * So be sure you know what you are doing!
     * @param item
     */
    protected void dropItem(I item) {
        mCache.remove(item.mDescriptor);
        mRecentlyUsed.remove(item);
        onItemRemoved(item);
    } 
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
    public enum CleanUpPolicy {
        /** 
         * Remove items as soon as there are no handles left.
         */
        IMMEDIATELY,      
        /**
         * Items must be removed by derived classes via function 
         * freeLeastRecentlyUsed().
         */
        USER_DEFINED
    }
}
