package com.moonymango.snare.audio;

import java.io.IOException;

import com.moonymango.snare.audio.LoopHandle.HandleState;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.SparseArray;

public class SnareAudioManager implements OnLoadCompleteListener, 
        OnPreparedListener,
        OnAudioFocusChangeListener {
    
    private final SparseArray<SoundHandle> mSoundMap = new SparseArray<SoundHandle>();
    private final int mMaxStreams;
    private final Application mAppContext;
    private SoundPool mSoundPool;
    private MediaPlayer mMediaPlayer;
    private LoopHandle mCurrentLoop;
    private boolean mPlayerIsPreparing;
    private boolean mHasFocus;
    

    public SnareAudioManager (int maxStreams, Application appContext) {
        mMaxStreams = maxStreams;   
        mAppContext = appContext;
    }

    public void onPause() {
        final AudioManager audioManager = (AudioManager) mAppContext.getSystemService(Application.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        
        mSoundPool.release();
        mSoundPool = null;
        mSoundMap.clear();
        
        mMediaPlayer.release();
        mMediaPlayer = null;
        if (mCurrentLoop != null && mCurrentLoop.mState == HandleState.PLAYING) {
            mCurrentLoop.mState = HandleState.PLAY_ON_RESUME;
        }
    }

    public void onResume() {
        mSoundPool = new SoundPool(mMaxStreams, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(this);
        
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        if (mCurrentLoop != null) {
            // if loop was playing before onPause() then play it again ...
            if (mCurrentLoop.mState == HandleState.PLAY_ON_RESUME) {
                mCurrentLoop.play();
            } else { 
                mCurrentLoop.mState = HandleState.NOT_LOADED;
                mCurrentLoop = null;
            }
        }
        
        final AudioManager audioManager = (AudioManager) mAppContext.getSystemService(Application.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mHasFocus = true;
        }
    }

    public void onLoadComplete(SoundPool arg0, int soundID, int status) {
        final SoundHandle item = mSoundMap.get(soundID);
        if (status == 0 && item != null) {
            item.mIsLoaded = true;
        }
    }

    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            mHasFocus = true;
            mSoundPool.autoPause();
            mMediaPlayer.pause();
        } else {
            mHasFocus = false;   
            mSoundPool.autoResume();
            mMediaPlayer.start();
        } 
    }
    
    public void onPrepared(MediaPlayer arg0) {
        mPlayerIsPreparing = false;
        mMediaPlayer.setLooping(true);
        final float vol = mCurrentLoop.mVol;
        mMediaPlayer.setVolume(vol, vol);
        mMediaPlayer.start();
        mCurrentLoop.mState = HandleState.PLAYING;
    }

    protected void playSound(SoundHandle handle) {
        if (mHasFocus && mSoundPool != null) {
            final float vol = handle.mVol;
            mSoundPool.play(handle.mID, vol, vol, 0, 0, 1.0f);
        }
    }
    
    protected void setSoundVolume(SoundHandle handle) {
        final float vol = handle.mVol;
        mSoundPool.setVolume(handle.mID, vol, vol);
    }

    protected void loadSound(SoundResource descr, SoundHandle handle) throws IOException {
        final AssetFileDescriptor fd = mAppContext.getAssets().openFd(descr.getName());
        final int soundID = mSoundPool.load(fd, 1);
        fd.close();
        mSoundMap.put(soundID, handle);
        handle.mID = soundID;
    }

    protected void unloadSound(SoundHandle item) {
        final int id = item.mID;
        mSoundPool.unload(id);
        mSoundMap.remove(id);
        item.mIsLoaded = false;
    }

    protected void playLoop(LoopResource descr, LoopHandle handle) throws IOException {
        if (mPlayerIsPreparing) {
            throw new IllegalStateException("Still preparing loop.");
        }
        if (handle.equals(mCurrentLoop)) {
            switch(handle.mState) {
            case LOAD_COMPLETE:
                mMediaPlayer.start();
                handle.mState = HandleState.PLAYING;
            case PLAYING:
            case PREPARING:
                return;
            default:
            }
        } else if (mCurrentLoop != null) {
            unloadLoop(mCurrentLoop);
        }
        
        mCurrentLoop = handle;
        final AssetFileDescriptor afd = mAppContext.getAssets().openFd(descr.getName());
        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        afd.close();
        mMediaPlayer.prepareAsync();
        mPlayerIsPreparing = true;
        handle.mState = HandleState.PREPARING;
    }
    
    protected void pauseLoop(LoopHandle handle) {
        if (mPlayerIsPreparing) {
            throw new IllegalStateException("Still preparing loop.");
        }
        if (!handle.equals(mCurrentLoop)) {
            return;
        }
        handle.mState = HandleState.LOAD_COMPLETE;
        mMediaPlayer.pause();
    }
    
    protected void seekTo(LoopHandle handle, int msec) {
        if (mPlayerIsPreparing) {
            throw new IllegalStateException("Still preparing loop.");
        }
        if (!handle.equals(mCurrentLoop)) {
            return;
        }
        mMediaPlayer.seekTo(msec);
    }
    
    protected void setLoopVolume(LoopHandle handle) {
        if (mPlayerIsPreparing) {
            throw new IllegalStateException("Still preparing loop.");
        }
        if (!handle.equals(mCurrentLoop)) {
            return;
        }
        mMediaPlayer.setVolume(handle.mVol, handle.mVol);
    }
    
    protected void unloadLoop(LoopHandle handle) {
        if (mPlayerIsPreparing) {
            throw new IllegalStateException("Still preparing loop.");
        }
        if (!handle.equals(mCurrentLoop)) {
            return;
        }
        mCurrentLoop.mState = HandleState.NOT_LOADED;
        mMediaPlayer.reset();
        mCurrentLoop = null;
    }
    
}
