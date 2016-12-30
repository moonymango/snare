package stachelsau.snare.audio;

import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.ComponentType;
import stachelsau.snare.game.GameObj.IComponent;

/**
 * Class that allows annotation of game objects with a sound resource
 */
public class AudioComponent implements IComponent {

    private final SoundResource mDescr;
    private SoundHandle mSoundHnd;
    private GameObj mGameObj;
    
    public AudioComponent(SoundResource descr) {
        mDescr = descr;
    }

    public ComponentType getComponentType() {
        return ComponentType.AUDIO;
    }

    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}
    public void onTraverse(Object userData) {}
    public GameObj getGameObj() {return mGameObj;}
    public void setGameObj(GameObj obj) {mGameObj = obj;}
    
    public void onInit() {
        mSoundHnd = mDescr.getHandle();
    }
    
    public void onShutdown() {
        mDescr.releaseHandle(mSoundHnd);
        mSoundHnd = null;
    }

    public void reset() {}

    public void play() {
        if (mSoundHnd != null) {
            mSoundHnd.play();
        }
    }
    
}
