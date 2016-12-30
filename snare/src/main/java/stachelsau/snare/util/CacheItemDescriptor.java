package stachelsau.snare.util;

import stachelsau.snare.game.Game;

public abstract class CacheItemDescriptor<C extends Cache<C,D,I>, D extends CacheItemDescriptor<C,D,I>, I extends CacheItem<C,D,I>> {

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    /** Name of the cache item */
    protected final String mName;
    /** Additional and optional qualifier for the cache item. Might be null. */ 
    protected final String mQualifier;
    /** Qualified name of the cache item. Concatenation of name and qualifier. */
    protected final String mQName;
    protected C mCache;
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public CacheItemDescriptor(String name, String qualifier) {
        if (name == null) {
            throw new IllegalArgumentException("Missing cache item descriptor name.");
        }
        mName = name;
        if (qualifier != null) {
            mQualifier = qualifier;
            mQName = name + Game.DELIMITER + mQualifier;
        } else {
            mQualifier = null;
            mQName = name;
        }
    }
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    protected I getHandle(C cache) {
        if (cache == null) {
            throw new IllegalArgumentException("Missing cache.");
        }
        if (mCache != null && mCache != cache) {
            throw new IllegalStateException("Cache item descriptor already bound to another cache.");
        }
        mCache = cache;
        return mCache.getHandle(this);
    }
    
    public void releaseHandle(I item) {
        mCache.releaseHandle(item);
    }
    
    public String getName() {
        return mName;
    }
    
    public String getQualifier() {
        return mQualifier;
    }
    
    public String getQName() {
        return mQName;
    }
     
    protected abstract I createCacheItem();
     
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof CacheItemDescriptor<?,?,?>)) return false;
        return this.hashCode() == o.hashCode();
    }

    @Override
    public int hashCode() {
        return mQName.hashCode();
    }

    @Override
    public String toString() {
        return mQName;
    }
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
