/**
 * 
 */
package com.moonymango.snare.ui;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IFlingEvent;
import com.moonymango.snare.events.IKeyEvent;
import com.moonymango.snare.events.IScaleEvent;
import com.moonymango.snare.events.IScrollEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.BaseGameView;
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.ui.widgets.BaseTouchWidget.TouchSetting;
import com.moonymango.snare.ui.widgets.BaseWidget.PositionAlignment;
import com.moonymango.snare.ui.widgets.Text;
import com.moonymango.snare.util.RingBuffer;

import java.util.ArrayList;

/**
 * Base class for player views. This class has two aspects: <br/>
 * 1. Drawing of player view. <br/>
 * 2. Handling of user input. <br/>
 * 
 * Note: All input related callbacks must not trigger actions directly
 * because they are called from the UI thread. Instead the event is queued
 * from the onUpdate() callback, which runs in the game loop. Current
 * implementation stores only one event. Further events that arrive
 * until next onUpdate() will be discarded! (This does not work with
 * scale events) 
 */
public class PlayerGameView extends BaseGameView implements IEventListener, 
        OnTouchListener,
        OnDoubleTapListener,
        OnGestureListener,
        OnScaleGestureListener,
        OnKeyListener {
  
    // ---------------------------------------------------------
    // static
    // ---------------------------------------------------------
    private static final int DEBUG_PRINT_LINES = 40;
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    private int mScreenWidth = 1;
    private int mScreenHeight = 1;
    private float mScreenRatio = 1;
    
    private final ArrayList<IScreenElement> mScreenElems = new ArrayList<IScreenElement>();
    private boolean mIsInitialized;
    private Text mDebugText;
    private boolean mDebugStringsChanged;
    private RingBuffer<String> mDebugStrings;
    private int mDebugPrintFontSize = 1;
    
    private ScaleGestureDetector mSGDetector;
    private boolean mScaleInProgress;
    private GestureDetector mGDetector;
    
    //private DefaultEvent mEvent;
    //private final DefaultEventPool mEventPool = new DefaultEventPool();

        
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    public PlayerGameView(IGame game)
    {
        super(game);
    }

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    /**
     * Push a screen element onto the stack.
     * @param elem
     */
    public void pushScreenElement(IScreenElement elem) {
        if (!elem.isAttached()) { 
            mScreenElems.add(elem);
            if (mIsInitialized) {
                elem.onAttachToScreen(this, mScreenWidth, mScreenHeight);
            }
        }
    }
    
    /**
     * Pop screen element from the stack.
     * @return
     */
    public IScreenElement popScreenElement() {
        IScreenElement elem = mScreenElems.get(mScreenElems.size()-1);
        mScreenElems.remove(elem);
        if (mIsInitialized) {
            elem.onDetachFromScreen();
        }
        return elem;
    }
    
    public void popAllScreenElements() {
        while (!mScreenElems.isEmpty()) {
            popScreenElement();
        }
    }
    
    public void removeScreenElement(IScreenElement elem) {
        if (elem.isAttached()) {
            mScreenElems.remove(elem);
            if (mIsInitialized) {
                elem.onDetachFromScreen();
            }
        }
    }
           
    public void onSurfaceChanged(int width, int height){
        if (mScreenWidth == width && mScreenHeight == height) { 
            return;
        }
        mScreenWidth = width;
        mScreenHeight = height;
        mScreenRatio = (float) width / height;
        mDebugPrintFontSize = height/DEBUG_PRINT_LINES;
        
        if (!mIsInitialized) {
            return;
        }
        for (IScreenElement e: mScreenElems) {
            e.onAttachToScreen(this, mScreenWidth, mScreenHeight);
        }
        if (mDebugText != null) {
            mDebugText.setPosition(0, height);
            mDebugText.onAttachToScreen(this, mScreenWidth, mScreenHeight);
            mDebugText.setTextSize(mDebugPrintFontSize);
        }
    }

    public void draw() {
        // first draw elements from bottom to top of stack
        int len = mScreenElems.size();
        for (int i = 0; i < len; i++){
           IScreenElement elem = mScreenElems.get(i); 
           if (elem.isVisible()) {
               elem.draw(); 
           }
        } 
        
        if (mDebugText != null) {
            mDebugText.draw();
        }       
    }

    public final boolean onTouch(View v, MotionEvent event) {
        if (!mIsInitialized) {
            return false;
        }
        if (mGame.getSettings().INPUT_EVENT_MASK.SCALE_ENABLED) {
            mSGDetector.onTouchEvent(event);
        }

        return mSGDetector.isInProgress() || mGDetector.onTouchEvent(event);
    }

    public boolean handleEvent(IEvent event) {
        if(!event.getType().equals(ITouchEvent.EVENT_TYPE)) {
            return false;
        }
        // distribute touch event to screen elements from top to bottom
        final ITouchEvent e = (ITouchEvent) event;
        for (int i = mScreenElems.size()-1; i >= 0; i--) {
            final IScreenElement se = mScreenElems.get(i);
            final int x = e.getTouchX();
            final int y = e.getTouchY();
            if (se.containsCoord(x, y) && se.isVisible() && se.onTouchEvent(e)) {
                break;
            }
        }
        return false;                  
    }
    
    /**
     * Clears the debug output ring buffer.
     */
    public void debugPrintClear() {
        if (mDebugStrings != null) {
            mDebugStrings.clear();
            mDebugStringsChanged = true;
        }
    }
    
    /** 
     * Add a string to debug output ring buffer.
     * @param msg
     */
    public void debugPrint(String msg) {
        if (mDebugStrings != null) {
            mDebugStrings.add(msg);
            mDebugStringsChanged = true;
        }
    }
    
    /**
     * Height of screen in pixels. 
     * Note: screen coordinates (0, 0) = lower left corner
     * @return
     */
    public int getScreenHeight() {
        return mScreenHeight;
    }
    
    /**
     * Width of screen in pixels.
     * Note: screen coordinates (0, 0) = lower left corner 
     * @return
     */
    public int getScreenWidth() {
        return mScreenWidth;
    }
    
    public float getScreenRatio() {
        return mScreenRatio;
    }
    
    /**
     * Calculates absolute screen coordinate from a relative one.
     * Relative coords of the visible screen are in [0..1].
     * (0 = left, 1 = right)
     * @param rel
     * @return
     */
    public int getScreenCoordX(float rel) {
        return (int) (mScreenWidth * rel);
    }
    
    /**
     * Calculates absolute screen coordinate from a relative one.
     * Relative coords of the visible screen are in [0..1].
     * (0 = bottom, 1 = top)
     * @param rel
     * @return
     */
    public int getScreenCoordY(float rel) {
        return (int) (mScreenHeight * rel);
    }
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
       
    @Override
    public void onInit() {
        int len = mScreenElems.size();
        for (int i = 0; i < len; i++) { 
            mScreenElems.get(i).onAttachToScreen(this, mScreenWidth, mScreenHeight);
        }
        
        // register events
        mGame.getEventManager().addListener(ITouchEvent.EVENT_TYPE, this);
        
        // load system font and set up debug text field
        final BaseFont font = mGame.getSystemFont();
        if (font != null) {
            mDebugStrings = new RingBuffer<String>(DEBUG_PRINT_LINES);
            mDebugText = new Text(font, null, PositionAlignment.LEFT_X_TOP_Y, TouchSetting.NOT_TOUCHABLE, 256);
            mDebugText.setTextSize(mDebugPrintFontSize);
            mDebugText.setColor(1, 1, 1, 0.4f);
            mDebugText.setPosition(0, mScreenHeight); // output in upper left corner
            mDebugText.onAttachToScreen(this, mScreenWidth, mScreenHeight);
        }
        
        final Handler h = new Handler(mGame.getApplication().getMainLooper());

        // gesture detectors
        mSGDetector = new ScaleGestureDetector(mGame.getApplication(), this, h);

        mGDetector = new GestureDetector(mGame.getApplication(), this, h);
        mGDetector.setIsLongpressEnabled(false); // longpress not useful for games
        mGDetector.setOnDoubleTapListener(this);
        
        mIsInitialized = true;   
    }
    
    
    @Override
    public void onShutdown() {
        int len = mScreenElems.size();
        for (int i = 0; i < len; i++) { 
            mScreenElems.get(i).onDetachFromScreen();
        }
    
        mGame.getEventManager().removeListener(ITouchEvent.EVENT_TYPE, this);
                
        if (mDebugText != null) {
            mDebugStrings = null;
            mDebugText.onDetachFromScreen();
            mDebugText = null;
        }
        
        mSGDetector = null;
        mGDetector = null;
        
        mIsInitialized = false;
    }

    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {
        
        // schedule pending input event
        //if (mEvent != null) {
        //    Game.get().getEventManager().queueEvent(mEvent);
        //    mEvent = null;
        //}
        
        // prepare debug print string for drawing
        if (mDebugStringsChanged && mDebugText != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mDebugStrings.getSize(); i++) {
                String s = mDebugStrings.get(i);
                if (s != null) {
                    sb.append(s);
                }
            }
            mDebugText.setText(sb.toString());
            mDebugStringsChanged = false;
        }
    }
    
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        /*if (mEvent != null) {
            return false;
        }*/
        
        final GameSettings s = mGame.getSettings();
        if (keyCode == KeyEvent.KEYCODE_BACK 
                && !s.mCustomBackButton) {
            // let activity do default action
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP 
                && !s.mCustomVolumeUpButton) {
            // let activity do default action
            return false;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN 
                && !s.mCustomVolumeDownButton) {
            // let activity do default action
            return false;
        }
        
        final EventManager em = mGame.getEventManager();
        final IKeyEvent e = em.obtain(IKeyEvent.EVENT_TYPE);
        e.setKeyData(keyCode, event.getAction());
        em.queueEvent(e);
        
        return true;
    }

    /**
     * Override to handle scale gestures. Default implementation
     * creates event {@link IScaleEvent}.
     */
    public boolean onScale(ScaleGestureDetector detector) {
        
        if (!mGame.getSettings().INPUT_EVENT_MASK.SCALE_ENABLED) {
            return true;
        }
        final EventManager em = mGame.getEventManager();
        final IScaleEvent ev = em.obtain(IScaleEvent.EVENT_TYPE);
        ev.setScaleData(detector.getScaleFactor(), !mScaleInProgress);
        em.queueEvent(ev);
        
        mScaleInProgress = true;
        return false;
    }
    
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mGame.getSettings().INPUT_EVENT_MASK.SCALE_ENABLED;
    }

    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleInProgress = false;
    }

    /**
     * Override to handle pointer down. Default implementation creates event 
     * {@link ITouchEvent}.
     */
    public boolean onDown(MotionEvent e) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.DOWN_ENABLED) {
            // other GestureListener callbacks don't fire, if we return false here
            return true;
        }
        final EventManager em = mGame.getEventManager();
        final ITouchEvent ev = em.obtain(ITouchEvent.EVENT_TYPE);
        ev.setTouchData(TouchAction.DOWN, (int) e.getX(), 
                mScreenHeight - (int) e.getY());
        em.queueEvent(ev);
                
        return true;
        
    }

    /**
     * Override to handle fling gestures. Default implementation creates event 
     * {@link IFlingEvent}.
     */
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.FLING_ENABLED) {
            return false;
        }
        final EventManager em = mGame.getEventManager();
        final IFlingEvent ev = em.obtain(IFlingEvent.EVENT_TYPE);
        ev.setFlingData((int) e1.getX(), mScreenHeight - (int) e1.getY(), 
                velocityX, velocityY);
        em.queueEvent(ev);
        
        return true;
    }

    public void onLongPress(MotionEvent e) {
        // long press not enabled
    }

    /**
     * Override to handle scroll gestures. Default implementation creates event 
     * {@link IScrollEvent}.
     */
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
            float distanceY) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.SCROLL_ENABLED) {
            return false;
        }
        final EventManager em = mGame.getEventManager();
        final IScrollEvent ev = em.obtain(IScrollEvent.EVENT_TYPE);
        ev.setScrollData((int) e1.getX(), mScreenHeight - (int) e1.getY(), 
                distanceX, distanceY);
        em.queueEvent(ev);
        
        return true;
    }

    /**
     * Override to handle show press. Default implementation creates event 
     * {@link ITouchEvent}.
     */
    public void onShowPress(MotionEvent e) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.SHOW_PRESS_ENABLED) {
            return;
        }
        final EventManager em = mGame.getEventManager();
        final ITouchEvent ev = em.obtain(ITouchEvent.EVENT_TYPE);
        ev.setTouchData(TouchAction.SHOW_PRESS, (int) e.getX(), 
                mScreenHeight - (int) e.getY());
        em.queueEvent(ev);
    }

    /**
     * Override to handle single tap up. Default implementation creates event 
     * {@link ITouchEvent}.
     */
    public boolean onSingleTapUp(MotionEvent e) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.SINGLE_TAP_UP_ENABLED) {
            return false;
        }
        final EventManager em = mGame.getEventManager();
        final ITouchEvent ev = em.obtain(ITouchEvent.EVENT_TYPE);
        ev.setTouchData(TouchAction.SINGLE_TAB_UP, (int) e.getX(), 
                mScreenHeight - (int) e.getY());
        em.queueEvent(ev);
        
        return true;
    }

    /**
     * Override to handle double tap. Default implementation creates event 
     * {@link ITouchEvent}.
     */
    public boolean onDoubleTap(MotionEvent e) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.DOUBLE_TAP_ENABLED) {
            return false;
        }
        final EventManager em = mGame.getEventManager();
        final ITouchEvent ev = em.obtain(ITouchEvent.EVENT_TYPE); 
        ev.setTouchData(TouchAction.DOUBLE_TAB, (int) e.getX(), 
                mScreenHeight - (int) e.getY());
        em.queueEvent(ev);
        
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    /**
     * Override to handle single tap confirmed. Default implementation creates event 
     * {@link ITouchEvent}.
     */
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (!mGame.getSettings().INPUT_EVENT_MASK.SINGLE_TAP_ENABLED) {
            return false;
        }
        final EventManager em = mGame.getEventManager();
        final ITouchEvent ev = em.obtain(ITouchEvent.EVENT_TYPE); 
        ev.setTouchData(TouchAction.SINGLE_TAB, (int) e.getX(), 
                mScreenHeight - (int) e.getY());
        em.queueEvent(ev);
                
        return true;
    }
    
    
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
