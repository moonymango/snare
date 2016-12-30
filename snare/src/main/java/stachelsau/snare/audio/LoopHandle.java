package stachelsau.snare.audio;

import java.io.IOException;

import stachelsau.snare.res.BaseResHandle;

public class LoopHandle extends BaseResHandle {

    /** Indicate that this is loop is currently loaded by MediaPlayer */
    //protected boolean mIsCurrent;
    /** Loop is actually playing */
    //protected boolean mIsPlaying;
    
    protected HandleState mState = HandleState.NOT_LOADED;
    
    protected float mVol;
    private final SnareAudioManager mAudioManager;
    
    public LoopHandle(LoopResource res, SnareAudioManager am, float vol) {
        super(res);
        mAudioManager = am;
        mVol = vol;
    }
    
    /**
     * Play the loop. Only one loop can be played at a time, 
     * so this will possibly cancel another playing loop.
     */
    public void play() {
        if (mState == HandleState.PLAYING) {
            return;
        }
        try {
            mAudioManager.playLoop((LoopResource)mDescriptor, this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Indicates that loop is playing.
     * @return
     */
    public boolean isPlaying() {
        return mState == HandleState.PLAYING;
    }
    
    /**
     * Indicates that this is the current loop for playback.
     * @return
     */
    public boolean isCurrent() {
        return mState != HandleState.NOT_LOADED;
    }
    
    /** Indicates that loop is being prepared for playback by MediaPlayer */
    public boolean isPreparing() {
        return mState == HandleState.PREPARING;
    }
    
    public void setVolume(float vol) {
        mVol = vol;
        mAudioManager.setLoopVolume(this);
    }
    
    public void seekTo(int msec) {
        if (mState == HandleState.PLAYING || mState == HandleState.LOAD_COMPLETE) {
            mAudioManager.seekTo(this, msec);
        }
    }
    
    public void pause() {
        if (mState == HandleState.PLAYING) {
            mAudioManager.pauseLoop(this);
        }
    }

    @Override
    public boolean onRemoveFromCachePending() {
        if (mState != HandleState.NOT_LOADED) {
            mAudioManager.unloadLoop(this);
        }
        return true;
    }
    
    public static enum HandleState {
        /** loop is not loaded and not playing */
        NOT_LOADED,     
        /** MediaPlayer is preparing */
        PREPARING,      
        /** loaded but not playing (stopped or paused) */
        LOAD_COMPLETE,  
        /** Playing */
        PLAYING,      
        /** marks handle to be played in onResume() */
        PLAY_ON_RESUME  
    }
    
}
