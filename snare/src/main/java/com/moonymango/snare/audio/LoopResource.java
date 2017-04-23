package com.moonymango.snare.audio;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.BaseResource;
import com.moonymango.snare.res.IAssetName;

public class LoopResource extends BaseResource {
    
    public LoopResource(IGame game, IAssetName asset) {
        super(game, asset);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        return new LoopHandle(this, SnareGame.get().getAudioManager(),
                SnareGame.get().getSettings().mDefaultLoopVolume);
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // never called
        return null;
    }
    
    public LoopHandle getHandle() {
        return (LoopHandle) getHandle(SnareGame.get().getResourceCache());
    }

}
