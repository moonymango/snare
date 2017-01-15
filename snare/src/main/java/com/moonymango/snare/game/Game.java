package com.moonymango.snare.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Debug;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.SparseArray;
import android.widget.Toast;

import com.moonymango.snare.audio.SnareAudioManager;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.IGameObjDestroyEvent;
import com.moonymango.snare.events.IGameObjNewEvent;
import com.moonymango.snare.events.IGameStateChangedEvent;
import com.moonymango.snare.events.IStatsUpdateEvent;
import com.moonymango.snare.game.GameObj.GameObjLayer;
import com.moonymango.snare.opengl.GLObjCache;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.proc.ProcessManager;
import com.moonymango.snare.res.ResourceCache;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.util.Logger;
import com.moonymango.snare.util.Logger.LogSource;
import com.moonymango.snare.util.RandomString;

import java.util.ArrayList;
import java.util.Random;

/**
 * Container for everything that belongs to the engine.
 * 
 * We have two clocks that can be used throughout the game:
 * 1. Real Time: 
 * This clock runs at the same speed as the system's clock with one
 * exception. When the delta between two frames exceeds the value of 
 * REALTIME_DELTA_THRESHOLD (see {@link GameSettings}), e.g. because the system
 * is doing something else in the background, then the delta is set to 0.
 * This mitigates "jumps". If debugger is connected, the the delta between two 
 * frames frozen to the value of V_REALTIME_DELTA_WHEN_DEBUGGER_CONNECTED.
 *   
 * 2. Virtual Time:
 * This clock is derived from above Real Time and a factor. By adjusting this
 * factor it is possible to let the game run slower or faster. Additionally
 * this clock can be explicitly stopped, which is not possible for real time.
 * 
 * TODO prioB: set UncaughtExceptionHandler
 */
public final class Game {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    
    /*static {    
        System.loadLibrary("snare");
    }*/
    
    public static final String ENGINE_NAME = "snare";
    public static final String DELIMITER = ".";

    // game instance is bound to an activity and vice versa,
    // when activity gets destroyed the game instance gets released,
    // static reference for easy access to game instance
    @SuppressLint("StaticFieldLeak")
    private static Game sInstance = null;

    /** Getter for use from the activity only */
    protected static Game create(BaseGameActivity activity) {
        // create new game instance
        final String name = activity.getName();
        final GameSettings settings = activity.onLoadGameSettings();
        final PlayerGameView view = activity.onLoadPrimaryPlayerView();
        final IRenderer renderer = activity.onLoadRenderer(view);
        final EventManager em = activity.onLoadEventManager();
        final IPhysics physics = activity.onLoadPhysics();      
        sInstance = new Game(name, activity, view,
                physics, renderer, settings, em);
                
        return sInstance;
    }
    
    public static Game get() {
        return sInstance;
    }

    public static void dispose() {
        sInstance = null;
    }
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    private final String mName;
    private final EventManager mEventManager;
    private final ResourceCache mRessourceCache;
    private final SnareAudioManager mAudioManager;
    private final BaseGameActivity mActivity;
    private final IPhysics mPhysics;
    private final PlayerGameView mPrimaryPlayerView;
    private final GameSettings mSettings;
    private GLSurfaceView mSurfaceView;
    private final IRenderer mRenderer;
    private final Vibrator mVibrator;
    private final ProcessManager mProcessManager = new ProcessManager();
    private final GLObjCache mGLObjCache = new GLObjCache();
    private final Random mRandom = new Random();
    private final RandomString mRandomStringGen;
    
    private final ArrayList<GameObj> mObjectsList = new ArrayList<GameObj>();
    private final SparseArray<GameObj> mObjects = new SparseArray<GameObj>();
    private final ArrayList<BaseGameView> mViewsList = new ArrayList<BaseGameView>();
    private final SparseArray<BaseGameView> mViews = new SparseArray<BaseGameView>();
    
    private IGameState mGameState;
    private IGameState mPrevGameState;
    private IGameState mPrevPrevGameState;
    private IGameState mPauseGameState;
    private IGameStateLogic mLogic;
    private Object mScratchPad;
    private BaseFont mSystemFont;
    private boolean mVirtualTimeStopped;
    private float mVirtualTimeFactor = 1;
    private long mRealtime; 
    private long mLastMeasuredTime;
    
    private float mLastFPS;
    private float mMinDelta;
    private float mMaxDelta;
    
    private GameThread mGameThread;
    private boolean mInitDone = false;
    private boolean mIsDrawing;
    protected float mDummy;
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    private Game(String name,
            BaseGameActivity activity,
            PlayerGameView view,
            IPhysics physics,
            IRenderer renderer, 
            GameSettings settings,
            EventManager eventManager) {

        final Application app = activity.getApplication();

        mName = name;
        mActivity = activity;
        mEventManager = eventManager;
        mRessourceCache = new ResourceCache(settings.RESOURCE_CACHE_THRESHOLD, app);
        mSettings = settings;
        mPhysics = physics;
        mRenderer = renderer;
        mPrimaryPlayerView = view;
        mAudioManager = new SnareAudioManager(settings.SOUND_MAX_STREAMS, app);
        mVibrator = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
        mRandomStringGen = new RandomString(16, this);
    }
    
    
    GLSurfaceView prepareGLSurfaceView() {
        mSurfaceView = new GLSurfaceView(mActivity.getApplication());
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setEGLConfigChooser(mSettings.RENDER_OPTIONS.EGL_RED_SIZE,
                mSettings.RENDER_OPTIONS.EGL_GREEN_SIZE,
                mSettings.RENDER_OPTIONS.EGL_BLUE_SIZE,
                mSettings.RENDER_OPTIONS.EGL_ALPHA_SIZE, 
                mSettings.RENDER_OPTIONS.EGL_DEPTH_SIZE,
                mSettings.RENDER_OPTIONS.EGL_STENCIL_SIZE);
        mSurfaceView.setRenderer(mRenderer);     
        mSurfaceView.setOnTouchListener(mPrimaryPlayerView);
        mSurfaceView.setFocusableInTouchMode(true); 
        mSurfaceView.setOnKeyListener(mPrimaryPlayerView);
        return mSurfaceView;
    }

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public synchronized void waitForDraw() {
        if (!mSettings.MULTI_THREADED) {
            update();
            return;
        }
        while (!mIsDrawing) {
            try {
                wait();
            } catch (InterruptedException e) {
                // handled by GLSurfaceView.onPause, nothing to do here
            }
        }
    }
    
    public synchronized void notifyEndDraw() {
        if (!mSettings.MULTI_THREADED) {
            return;
        }
        mIsDrawing = false;
        notifyAll();
    }
    
    private synchronized void waitForUpdate() throws InterruptedException {
        if (!mSettings.MULTI_THREADED) {
            return;
        }
        while (mIsDrawing) {
            wait();
        }
    }
    
    private synchronized void notifyEndUpdate() {
        if (!mSettings.MULTI_THREADED) {
            return;
        }
        mIsDrawing = true;
        notifyAll();
    }
    
    
    public void onResume() {
        mLastMeasuredTime = SystemClock.elapsedRealtime();
        mAudioManager.onResume();    // create new SoundPool
        mRessourceCache.onResume();  // load sound resources to new pool
        
        if (mSettings.MULTI_THREADED && mGameThread == null) {
            // configured for multi threading and no game thread available
            mGameThread = new GameThread();
            mGameThread.setPriority(Thread.MAX_PRIORITY);
            mGameThread.setName("SnareGameLoop");
            mGameThread.start();
        }
    }
    
    
    public void onPause() {
        if (mSettings.MULTI_THREADED && mGameThread != null) {
            mGameThread.interrupt();
            try {
                mGameThread.join();
            } catch (InterruptedException e1) { }
            mGameThread = null;
        }
        setGameState(mPauseGameState);
        
        // release sound resources
        mAudioManager.onPause(); 
        mRessourceCache.onPause();
    }
    
    public String getName()                     {return mName;}
    public EventManager getEventManager()       {return mEventManager;}
    public ResourceCache getResourceCache()     {return mRessourceCache;}
    public SnareAudioManager getAudioManager()  {return mAudioManager;}
    public IRenderer getRenderer()              {return mRenderer;}
    public PlayerGameView getPrimaryView()      {return mPrimaryPlayerView;}
    public GameSettings getSettings()           {return mSettings;}
    public BaseFont getSystemFont()             {return mSystemFont;}
    public void setScratchPadData(Object data)  {mScratchPad = data;}
    public Object getScratchPadData()           {return mScratchPad;}
    public String getRandomString()             {return mRandomStringGen.nextString();}
    public int getRandomInt()                   {return mRandom.nextInt();}
    public int getRandomInt(int min, int max) {
        return mRandom.nextInt(max - min + 1) + min;
    }
    /**
     * @return Random float in [0..1];
     */
    public float getRandomFloat() {
        return mRandom.nextFloat();
    }
    /**
     * @return Random float in [min..max];
     */
    public float getRandomFloat(float min, float max) {
        return mRandom.nextFloat() * (max - min) + min;
    }
    /**
     * Returns a resource string like {@link Activity}.getString()
     * @param resId
     * @return
     */
    public String getResourceString(int resId) {
        return mActivity != null ? mActivity.getApplication().getString(resId) : null;
    }
    /**
     * @return retrieves {@link SharedPreferences} from the context. 
     */
    public SharedPreferences getPreferences(String name) {
        if (mActivity != null) {
            return mActivity.getSharedPreferences(name, Context.MODE_PRIVATE);
        } else {
            return null;
        }
    }

    public Application getApplication() {
        return mActivity != null ? mActivity.getApplication(): null;
    }
    
    public GLSurfaceView getSurfaceView()       {return mSurfaceView;}
    public ProcessManager getProcManager()      {return mProcessManager;}
    public GLObjCache getGLObjCache()           {return mGLObjCache;}
    /** Returns active game state. */
    public IGameState getGameState()            {return mGameState;}
    /** Returns previous game state. */
    public IGameState getPrevGameState()        {return mPrevGameState;}
    /** Returns state before previous game state. */
    public IGameState getPrevPrevGameState()    {return mPrevPrevGameState;}
    public IGameState getOnPauseGameState()     {return mPauseGameState;}
    public IPhysics getPhysics()                {return mPhysics;}
    public void stopVirtualTime()               {mVirtualTimeStopped = true;}
    public void startVirtualTime()              {mVirtualTimeStopped = false;}
    /** Returns last time taken from the system. */
    public long getLastMeasuredTime()           {return mLastMeasuredTime;}
    /** Returns game internal realtime. */
    public long getRealTime()                   {return mRealtime;}
    
    public void setVirtualTimeFactor(float factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Cannot turn back time.");
        }
        mVirtualTimeFactor = factor;
    }
    
    public float getVirtualTimeFactor()     {return mVirtualTimeFactor;}
    public void vibrate(long time)          {mVibrator.vibrate(time);}
    
    public void showToast(String msg) {
        if (mActivity == null) return;

        final String m = msg;
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(mActivity, m, Toast.LENGTH_SHORT).show();
            }
        });       
    }
    
    public GameObj addGameObj(GameObj obj) {
        mObjects.put(obj.getID(), obj);
        mObjectsList.add(obj);
        obj.onInit();
        
        IGameObjNewEvent e = mEventManager.obtain(IGameObjNewEvent.EVENT_TYPE);
        e.setGameObj(obj);
        mEventManager.triggerEvent(e); 
        //Logger.i(LogSource.GAME, "added game object id " + obj.getID());
        return obj;
    }
    
    public void removeGameObj(GameObj obj) {
        IGameObjDestroyEvent e = mEventManager.obtain(IGameObjDestroyEvent.EVENT_TYPE);
        e.setGameObj(obj);
        mEventManager.triggerEvent(e); 
                
        mObjects.delete(obj.getID());
        mObjectsList.remove(obj);
        //Logger.i(LogSource.GAME, "removed game object id " + obj.getID());
        obj.onShutdown();   
    }
    
    public GameObj getObjById(int id) {
        return mObjects.get(id);
    }
    
    public GameObj getObjByListIdx(int idx) {
        return mObjectsList.get(idx);
    }
    
    /**
     * Gets game object by their list index. In case the object is not covered
     * by specified layer, null is returned.
     * @param idx
     * @param layer
     * @return
     */
    public GameObj getObjByListIdx(int idx, GameObjLayer layer) {
        final GameObj obj = mObjectsList.get(idx);
        return layer.covers(obj.getLayer()) ? obj : null;
    }
    
    /**
     * Gets total number of game objects.
     * @return
     */
    public int getGameObjCnt() {
        return mObjectsList.size();
    }
    
    public BaseGameView addGameView(BaseGameView view) {
        mViews.append(view.getID(), view);
        mViewsList.add(view);
        view.onInit();
        return view;
    }
    
    public void removeGameView(BaseGameView view) {
        mViews.delete(view.getID());
        mViewsList.remove(view);
        view.onShutdown();
    }
    
    private void setGameState(IGameState state) {
        if (state == null || state.equals(mGameState)) {
            return;
        }
        if (mGameState != null) {
            mGameState.getGameStateLogic().onDeactivate(state);
        }
        Logger.i(LogSource.GAME, "new game state: " + state.getName());
        mPrevPrevGameState = mPrevGameState;
        mPrevGameState = mGameState;
        mGameState = state;
        mLogic = state.getGameStateLogic();
        mLogic.onActivate(mPrevGameState);
        
        final IGameStateChangedEvent e = 
                mEventManager.obtain(IGameStateChangedEvent.EVENT_TYPE);
        e.setGameStateData(mGameState, mPrevGameState);
        mEventManager.queueEvent(e);
    }
    
    /**
     * Sets game state which will become active in case of a call to
     * the activity's onPause(). In this case the game state logic
     * is called like this:
     *   - onActivate() is called immediately from onPause()
     *   - after the app resumes, onUpdate() is called regularly (so this
     *     may be used to cover onResume())
     * @param state State or null in case that no state change should happen
     *               on pause
     */
    public void setOnPauseGameState(IGameState state) {
        mPauseGameState = state;
    }
    
    public Activity getActivity() {
        return mActivity;
    }
    
    public void showMessage(String msg, String buttonText, DialogInterface.OnClickListener listener) {
        final String m = msg;
        final DialogInterface.OnClickListener l = listener;
        final String t = buttonText != null ? buttonText : "Done";
        mActivity.runOnUiThread(new Runnable() {
            
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(mActivity).create();
                alertDialog.setMessage(m);
                if (l != null) {
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, t, l);
                }
                alertDialog.show();
            }
        });
    }
    
    /**
     * Updated game based on current system time.
     */
    private void update() {
        if (!mRenderer.hasSurface()) {
            return;
        }
        
        if (!mInitDone) {
            Logger.i(LogSource.GAME, "*** Hello, this is SNARE ***");
            
            mSystemFont = ((BaseGameActivity) mActivity).onLoadSystemFont();
            mRenderer.onInit();
            final int cnt = mRenderer.getPlayerViewCnt();
            for (int i = 0; i < cnt; i++) {
                addGameView(mRenderer.getPlayerViewByIdx(i));
            }
            mPhysics.onInit();
            
            // init game state logic last, so that renderer and physics will be ready to
            // catch newly created game objects
            setGameState(((BaseGameActivity) mActivity).onLoadInitialGameState());
            
            mInitDone = true;
        }
        
        final long t = SystemClock.elapsedRealtime();
        float delta = t - mLastMeasuredTime;
        mMaxDelta = Math.max(delta, mMaxDelta);
        mMinDelta = Math.min(delta, mMinDelta);
        mLastMeasuredTime = t;
        if (delta > mSettings.mRealtimeDeltaThreshold) {
            // Note: for debugging set this to delta which is equivalent to 50 fps to
            // see any time progress during debugging
            delta = Debug.isDebuggerConnected() ? 
                    mSettings.mRealtimeDeltaWhenDebuggerConnected : 
                        mSettings.mRealtimeDeltaThreshold;
        }
        mRealtime += delta;
        final float virtualDelta = mVirtualTimeStopped ? 0 : delta * mVirtualTimeFactor;
          
        mEventManager.tick(0);
        mPhysics.tick(mRealtime, delta, virtualDelta);
        mProcessManager.onUpdate(mRealtime, delta, virtualDelta);
        
        final IGameState state = mLogic.onUpdate(mRealtime, delta, virtualDelta);
        setGameState(state);
        
        // update game objects
        // TODO prioB: what happens when new objects are added during traversal??
        for (int i = mObjectsList.size()-1; i >= 0; i--) {
            mObjectsList.get(i).onUpdateComponents(mRealtime, delta, virtualDelta);
        }
        for (int i = mObjectsList.size()-1; i >= 0; i--) {
            mObjectsList.get(i).onUpdateTransform(mRealtime, delta, virtualDelta);
        }
        
        // update views
        for (int i = mViewsList.size()-1; i >= 0; i--) {
            mViewsList.get(i).onUpdate(mRealtime, delta, virtualDelta); 
        }
        
        if (mSettings.PRINT_STATS) {
            printStats();
        }
        
        dummyLoad();
    }
    
    private void dummyLoad() {
        // artificial processor load for debugging purposes
        mDummy = getRandomFloat(0, 10000);
        for (int i = mSettings.mDummyLoops; i >= 0; i--) {
            mDummy = (int) (mDummy < 200000 ? mDummy*3.5f : mDummy/2.78f);
        }
    }
    
    private void printStats() {
        final float fps = mRenderer.getFPS();
        
        if (fps != mLastFPS) {
            final int resCacheHandles = mRessourceCache.getHandlesCnt();
            final int resCacheItems = mRessourceCache.getItemCnt();
            final int glCacheHandles = mGLObjCache.getHandlesCnt();
            final int glCacheItems = mGLObjCache.getItemCnt();
            final int gameObjCnt = mObjectsList.size();
            final int procCnt = mProcessManager.getProcessCount();
            final int listenerCnt = mEventManager.getListenerCnt();
            
            // memory
            final int div = 1024 * 1024;
            final Runtime r = Runtime.getRuntime();
            final long max = r.maxMemory() / div;
            final long heap = r.totalMemory() / div;
            final long alloc = heap - r.freeMemory() / div;
            final long n = Debug.getNativeHeapSize() / div; 
            
            mRenderer.clear();
            String sFps = "FPS: " + fps + " " + mMaxDelta + " " + mMinDelta;
            Logger.i(LogSource.FPS, sFps);
            
            String s = sFps +
                    "\n " + resCacheHandles + " resource handles, " + resCacheItems + " items" + 
                    "\n " + glCacheHandles + " GL handles, " + glCacheItems + " items" +
                    "\n " + gameObjCnt + " game objects" +
                    "\n " + procCnt + " processes" +
                    "\n " + listenerCnt + " event listeners" +
                    "\nmem: " + max + " max, " + heap + " heap, " + alloc + " alloc, " + n + " native";
            mRenderer.print(s);
            
            // distribute FPS info
            IStatsUpdateEvent e = mEventManager.obtain(IStatsUpdateEvent.EVENT_TYPE);
            e.setStatsData(fps, mLastFPS, mMinDelta, mMaxDelta);
            mEventManager.queueEvent(e);

            mLastFPS = fps;
            mMaxDelta = Float.MIN_VALUE;
            mMinDelta = Float.MAX_VALUE;
        }
    }
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    
    // ---------------------------------------------------------
    // native
    // ---------------------------------------------------------
    /*public static native int sleep(long millis);
    public static native int lock();
    public static native int unlock();*/
        
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
    private class GameThread extends Thread {
                          
        @Override
        public void run() {
            Logger.i(LogSource.GAME, "game thread started.");            
            while(!isInterrupted()) {
                try {
                    waitForUpdate();
                } catch (InterruptedException e) {
                    interrupt();
                } 
                update();
                notifyEndUpdate();
            }    
            Logger.i(LogSource.GAME, "game thread stopped.");
        }
    }
    
    public enum ClockType {
        REALTIME,
        VIRTUAL
    }
    
}
