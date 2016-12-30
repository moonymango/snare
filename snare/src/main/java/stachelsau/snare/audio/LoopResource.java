package stachelsau.snare.audio;

import android.content.res.AssetManager;
import android.content.res.Resources;
import stachelsau.snare.game.Game;
import stachelsau.snare.res.BaseResHandle;
import stachelsau.snare.res.BaseResource;
import stachelsau.snare.res.IAssetName;

public class LoopResource extends BaseResource {
    
    public LoopResource(IAssetName asset) {
        super(asset);
    }

    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        return new LoopHandle(this, Game.get().getAudioManager(), 
                Game.get().getSettings().mDefaultLoopVolume);
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // never called
        return null;
    }
    
    public LoopHandle getHandle() {
        return (LoopHandle) getHandle(Game.get().getResourceCache());
    }

}
