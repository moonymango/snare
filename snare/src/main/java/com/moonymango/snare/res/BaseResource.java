package com.moonymango.snare.res;

import android.content.res.AssetManager;
import android.content.res.Resources;
import com.moonymango.snare.util.CacheItemDescriptor;
import com.moonymango.snare.util.Logger;
import com.moonymango.snare.util.Logger.LogSource;


public abstract class BaseResource extends CacheItemDescriptor<ResourceCache, BaseResource, BaseResHandle> {
    
    protected final boolean mUseAssets;
    protected final int mResID;
    protected final Class<? extends BaseResource> mResType;
    
    
    protected BaseResource(String name) {
        this(name, null);
    }
    
    protected BaseResource(String name, String qualifier) {
        super(name, qualifier);
        mUseAssets = true;
        mResID = 0;
        mResType = null; // resource type checking disabled
    }
    
    
    protected BaseResource(IAssetName asset) {
        super(asset.getName(), asset.getQualifier());
        mUseAssets = true;
        mResID = 0;
        mResType = asset.getType();
    }
    
    
    protected BaseResource(IResourceName res) {
        super(res.getName(), res.getQualifier());
        mUseAssets = false;
        mResID = res.getID();
        mResType = res.getType();
    }
    

    @Override
    protected BaseResHandle createCacheItem() {
        // check resource type
        if (mResType != null && !mResType.isInstance(this)) {
            throw new IllegalStateException("Resource type mismatch.");
        }
        Logger.i(LogSource.RESOURCES, "item created: " + mQName);
        if (mUseAssets) {
            return createHandleByAsset(mCache.getAssetManager());
        } else {
            return createHandleByResID(mCache.getResources());
        }
    }

    protected abstract BaseResHandle createHandleByAsset(AssetManager am);
    protected abstract BaseResHandle createHandleByResID(Resources res);
    
}
