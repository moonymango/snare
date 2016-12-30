package stachelsau.snare.res;

import stachelsau.snare.util.CacheItem;

public abstract class BaseResHandle extends CacheItem<ResourceCache, BaseResource, BaseResHandle> {
    
    
    public BaseResHandle(BaseResource res) {
        super(res);
    }

    @Override
    public void onAddedToCache() {}

    @Override
    public void onRefCntIncr() {}

    @Override
    public void onRefCntDecr() {}

    @Override
    public boolean onRemoveFromCachePending() {
        return true;
    }
    
    public void onPause() {}
    public void onResume() {}

}
