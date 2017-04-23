package com.moonymango.snare.audio;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.BaseResource;
import com.moonymango.snare.res.IAssetName;

import java.io.IOException;

public class SoundResource extends BaseResource {

    public SoundResource(IGame game, IAssetName asset) {
        super(game, asset);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        try {
            return new SoundHandle(this, SnareGame.get().getAudioManager(),
                    SnareGame.get().getSettings().mDefaultSoundFXVolume);
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
        return (SoundHandle) getHandle(SnareGame.get().getResourceCache());
    }

}
