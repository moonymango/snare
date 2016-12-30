package stachelsau.snare.audio;

import java.io.IOException;

import stachelsau.snare.game.Game;
import stachelsau.snare.res.BaseResHandle;
import stachelsau.snare.res.BaseResource;
import stachelsau.snare.res.IAssetName;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class SoundResource extends BaseResource {

    public SoundResource(IAssetName asset) {
        super(asset);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        try {
            final SoundHandle handle = new SoundHandle(this, Game.get().getAudioManager(),
                    Game.get().getSettings().mDefaultSoundFXVolume);
            return handle;
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
