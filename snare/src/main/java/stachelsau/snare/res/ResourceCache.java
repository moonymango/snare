package stachelsau.snare.res;

import stachelsau.snare.util.Cache;
import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;


/**
 * VM memory usage aware cache for all kinds of resources. Since it may not be
 * feasible to determine the size of a resource before actual creation, this
 * cache cannot guarantee that the memory usage will stay below a certain amount.
 * Instead it allows to define a memory usage threshold. A new resource will only 
 * be created when the actual memory usage is below the threshold.
 */
public class ResourceCache extends Cache<ResourceCache, BaseResource, BaseResHandle> {

    private final AssetManager mAssetManager;
    private final Resources mResources;
    private final long mThreshold;
    
    /**
     * New resource cache. Threshold value of 0 deactivates memory usage 
     * awareness.
     * @param threshold Memory usage threshold for creation of new resources.
     * @param appContext
     */
    public ResourceCache(long threshold, Application appContext) {
        super(CleanUpPolicy.USER_DEFINED);
        mResources = appContext.getResources();
        mAssetManager = appContext.getAssets();
        mThreshold = threshold;
    }  
    
    public AssetManager getAssetManager() {
        return mAssetManager;
    }
    
    public Resources getResources() {
        return mResources;
    }
    
    public void onPause() {
        for (int i = mRecentlyUsed.size() - 1; i >= 0; i--) {
            mRecentlyUsed.get(i).onPause();
        }
    }
    
    public void onResume() {
        for (int i = mRecentlyUsed.size() - 1; i >= 0; i--) {
            mRecentlyUsed.get(i).onResume();
        }
    }

    @Override
    protected boolean allowItemCreation(BaseResource res) {
        if (mThreshold <= 0) {
            return true;
        }
        final Runtime r = Runtime.getRuntime();
        long alloc = r.totalMemory()- r.freeMemory();
        while (alloc > mThreshold) {
            if (!freeLeastRecentlyUsed()) {
                return false;  // cannot free any more items
            }
            // TODO prioC: test corner case
            System.gc();
            alloc = r.totalMemory()- r.freeMemory();
        }
        return true;
    }
    
    
}
