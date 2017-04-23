package com.moonymango.snare.game;

import android.content.pm.ActivityInfo;
import android.media.SoundPool;

import com.moonymango.snare.events.IKeyEvent;
import com.moonymango.snare.opengl.RenderOptions;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.ui.InputEventMask;
import com.moonymango.snare.ui.scene3D.Scene3DOptions;

/**
 * Settings. Field names named upper case indicate a constant setting,
 * which is evaluated only once at startup. That means changing this
 * field after startup will have no effect. Fields named in lower case
 * (or camel case) may be changed any time to alter behaviour.
 */
public class GameSettings extends BaseSnareClass
{
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    
    // general
    /** Set true to enable status output in upper left corner of screen. */
    public boolean PRINT_STATS = false;  // printing stats will increase allocations in the game loop!!
    public boolean FULL_SCREEN = true;
    public boolean NO_TITLE = true;
    /** Set true to prevent device from sleeping. */
    public boolean KEEP_SCREEN_ON = true;
    /** Screen orientation. See {@link ActivityInfo}. */
    public int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    /** Set true to create separate thread for game loop. 
     * Otherwise all will be done in GL loop. */
    public boolean MULTI_THREADED = false;
    
    /** Max. delta between frames. When actual delta is greater than this, 
     * it will be clamped. */
    public long mRealtimeDeltaThreshold = 30;
    /** Delta between frames when debugger is connected. */
    public long mRealtimeDeltaWhenDebuggerConnected = 20;
    /** Creates additional load in game loop. Debugging puposes. */
    public int mDummyLoops = 0;
    /** Set to true to create {@link IKeyEvent} when back button is touched. */
    public boolean mCustomBackButton = false;
    /** Set to true to create {@link IKeyEvent} when vol up is touched. */
    public boolean mCustomVolumeUpButton = false;
    /** Set to true to create {@link IKeyEvent} when vol down is touched. */
    public boolean mCustomVolumeDownButton = false;
    
    // Resource cache
    /** Default cache threshold is VM maxMemory minus 4 MB */
    public long RESOURCE_CACHE_THRESHOLD = Runtime.getRuntime().maxMemory() - 4 *1024*1024;
    
    // Renderer
    public final RenderOptions RENDER_OPTIONS = new RenderOptions(); 
    public final TextureObjOptions mDefaultTextureOptions = TextureObjOptions.LINEAR_CLAMP;
    
    // 3D scene
    public final Scene3DOptions SCENE_OPTIONS;
    
    // sound
    /** Max number of streams in {@link SoundPool}. */
    public int SOUND_MAX_STREAMS = 10;
    public float mDefaultSoundFXVolume = 1;
    public float mDefaultLoopVolume = 0.3f;
    
    // input
    public final InputEventMask INPUT_EVENT_MASK = new InputEventMask();
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    public GameSettings(IGame game)
    {
        super(game);
        SCENE_OPTIONS = new Scene3DOptions(game);
    }


    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
    
}
