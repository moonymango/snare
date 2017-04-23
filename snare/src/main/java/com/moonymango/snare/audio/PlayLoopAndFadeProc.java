package com.moonymango.snare.audio;

import com.moonymango.snare.proc.ProcessManager.BaseProcess;

/**
 * Plays a loop for specified duration with fade-in and fade-out.
 */
public class PlayLoopAndFadeProc extends BaseProcess {
    
    private final LoopResource mRes;
    private LoopHandle mHandle;
    private float mFOFactor;
    private float mFIFactor;
    
    private float mInitVolume;
    private float mInitPDuration;
    private float mInitFODuration;
    private float mInitFIDuration;
        
    private float mVolume;
    private float mPDuration;
    private float mFODuration;
    private float mFIDuration;
    
    private State mState;
        
    /**
     * Constructor. Overall playback time is playDuration + fadeDuration.
     * @param res LoopResource to play.
     * @param volume Initial volume.
     * @param fadeInDuration Duration in seconds of fade-in.
     * @param playDuration Duration in seconds of playback at volume.
     * @param fadeOutDuration Duration in seconds of fade-out.
     */
    public PlayLoopAndFadeProc(LoopResource res, float volume, float fadeInDuration,
                               float playDuration, float fadeOutDuration)
    {
        super(res.mGame);
        mRes = res;
        mInitVolume = volume;
        mInitPDuration = playDuration * 1000;
        mInitFODuration = fadeOutDuration * 1000;
        mInitFIDuration = fadeInDuration * 1000;
        mFOFactor = 1 / mInitFODuration;
        mFIFactor = 1 / mInitFIDuration;
    }

    @Override
    protected void onInit() {
        mVolume = mInitVolume;
        mPDuration = mInitPDuration;
        mFODuration = mInitFODuration;
        mFIDuration = mInitFIDuration;
        mState = State.FADE_IN;
        mHandle = mRes.getHandle();
        mHandle.setVolume(0);
        mHandle.seekTo(0);  // reset playback position
        mHandle.play();

    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        if (!mHandle.isCurrent()) {
            // stop proc when another loop has replaced this one
            return false;
        }
        if (mHandle.isPreparing()) {
            // do nothing until loop is ready for playback
            return true;
        }
        
        switch (mState) {
        case FADE_IN:
            mFIDuration -= realDelta;
            if (mFIDuration < 0) {
                mHandle.setVolume(mVolume);
                mState = State.PLAYBACK;
            } else {
                mHandle.setVolume(mVolume * (1 - mFIFactor * mFIDuration));
            }
            break;
            
        case PLAYBACK:
            mPDuration -= realDelta;
            if (mPDuration < 0) {
                mState = State.FADE_OUT;
            }
            break;
            
        case FADE_OUT:
            mFODuration -= realDelta;
            if (mFODuration < 0) {
                // stop proc when fading is done
                return false;
            }
            mHandle.setVolume(mVolume * mFOFactor * mFODuration);
        }
     
        return true;
    }
    
    @Override
    protected void onKill() {
        mHandle.pause();
        mRes.releaseHandle(mHandle);
        mHandle = null;
    }

    private enum State {
        FADE_IN,
        PLAYBACK,
        FADE_OUT
    }

}
