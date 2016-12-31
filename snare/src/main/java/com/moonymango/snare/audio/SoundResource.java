package com.moonymango.snare.audio;

import java.io.IOException;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.BaseResource;
import com.moonymango.snare.res.IAssetName;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class SoundResource extends BaseResource {

    public SoundResource(IAssetName asset) {
        super(asset);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        try {
            return new SoundHandle(this, Game.get().getAudioManager(),
                    Game.get().getSettings().mDefaultSoundFXVolume);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // never called
        return null;
    }
    
    public SoundHandle getHandle() {
        return (SoundHandle) getHandle(Game.get().getResourceCache());
    }

}
