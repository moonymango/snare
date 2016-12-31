package com.moonymango.snare.res.texture;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.IAssetName;

import android.content.res.AssetManager;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;

public class ETC1TextureResource extends BaseTextureResource {

    public ETC1TextureResource(String name, ITextureRegionProvider provider) {
        super(name, provider);
    }

    public ETC1TextureResource(String name) {
        super(name);
    }

    public ETC1TextureResource(IAssetName asset) {
        super(asset);
    }
    
    public ETC1TextureResource(IAssetName asset,
            ITextureRegionProvider provider) {
        super(asset, provider);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        ETC1Texture t;
        try {
            BufferedInputStream in = new BufferedInputStream(am.open(mName));
            t = ETC1Util.createTexture(in);
            in.close();
        } catch (IOException e) {
            return null;
        } 
        if (t == null) {
            return null;
        }
        return new ETC1TextureResHandle(this, t);
    }

}
