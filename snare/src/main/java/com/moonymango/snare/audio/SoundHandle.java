package com.moonymango.snare.audio;

import java.io.IOException;

import com.moonymango.snare.res.BaseResHandle;

public class SoundHandle extends BaseResHandle {

    protected int mID;
    protected boolean mIsLoaded;
    protected float mVol;
    private final SnareAudioManager mAudioManager;
    
    public SoundHandle(SoundResource descr, SnareAudioManager am, float vol) throws IOException {
        super(descr);
        mAudioManager = am;
        am.loadSound(descr, this);
        mVol = vol;
    }
    
    public void play() {
        if (mIsLoaded) {
            mAudioManager.playSound(this);
        }
    }
    
    public void setVolume(float vol) {
        mVol = vol;
        mAudioManager.setSoundVolume(this);
    }
    
    @Override
    public boolean onRemoveFromCachePending() {
        mAudioManager.unloadSound(this);
        return true;
    }

    @Override
    public void onPause() {
        // audio manager releases sound pool, so we are not loaded any longer
        mIsLoaded = false;
    }

    @Override
    public void onResume() {
        // reload sound
        final SoundResource descr = (SoundResource) mDescriptor;
        try {
            mAudioManager.loadSound(descr, this);
        } catch (IOException e) {
            // will not happen because the sound was already loaded once
            // in the constructor
        }
    }

    
}
