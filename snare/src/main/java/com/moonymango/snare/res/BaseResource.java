package com.moonymango.snare.res;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.util.CacheItemDescriptor;
import com.moonymango.snare.util.Logger;
import com.moonymango.snare.util.Logger.LogSource;


public abstract class BaseResource extends CacheItemDescriptor<ResourceCache, BaseResource, BaseResHandle> {
    
    protected final int mResID;
    private final boolean mUseAssets;
    private final Class<? extends BaseResource> mResType;
    
    
    protected BaseResource(IGame game, String name) {
        this(game, name, null);
    }
    
    protected BaseResource(IGame game, String name, String qualifier) {
        super(game, name, qualifier);
        mUseAssets = true;
        mResID = 0;
        mResType = null; // resource type checking disabled
    }
    
    
    protected BaseResource(IGame game, IAssetName asset) {
        super(game, asset.getName(), asset.getQualifier());
        mUseAssets = true;
        mResID = 0;
        mResType = asset.getType();
    }
    
    
    protected BaseResource(IGame game, IResourceName res) {
        super(game, res.getName(), res.getQualifier());
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
