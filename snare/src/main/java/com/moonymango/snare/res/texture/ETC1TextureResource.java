package com.moonymango.snare.res.texture;

import android.content.res.AssetManager;
import android.opengl.ETC1Util;
import android.opengl.ETC1Util.ETC1Texture;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.IAssetName;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ETC1TextureResource extends BaseTextureResource {

    public ETC1TextureResource(IGame game, String name, ITextureRegionProvider provider)
    {
        super(game, name, provider);
    }

    public ETC1TextureResource(IGame game, String name) {
        super(game, name);
    }

    public ETC1TextureResource(IGame game, IAssetName asset) {
        super(game, asset);
    }
    
    public ETC1TextureResource(IGame game, IAssetName asset, ITextureRegionProvider provider)
    {
        super(game, asset, provider);
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
