
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
import com.moonymango.snare.events.IFlingEvent;
import com.moonymango.snare.events.IKeyEvent;
import com.moonymango.snare.events.IScaleEvent;
import com.moonymango.snare.events.IScrollEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGame;

/**
 * Player game view with input handling.
 *
 * Note: All input related callbacks must not trigger actions directly
 * because they are called from the UI thread. Instead an event is queued in
 * {@link EventManager}, so that the input can be made accessible to listeners in
 * the game loop.
 */
public class PlayerIOGameView extends PlayerGameView implements
        OnTouchListener,
        OnDoubleTapListener,
        OnGestureListener,
        OnScaleGestureListener,
        OnKeyListener
{

    private ScaleGestureDetector mSGDetector;
    private boolean mScaleInProgress;
    private GestureDetector mGDetector;

    //private DefaultEvent mEvent;
    //private final DefaultEventPool mEventPool = new DefaultEventPool();


    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    public PlayerIOGameView(IGame game)
    {
        super(game);
    }

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------

    public final boolean onTouch(View v, MotionEvent event)
    {
        if (!mIsInitialized) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            // call just for sake of completeness, normally there aren't any listeners
            v.performClick();
        }

        if (mGame.getSettings().INPUT_EVENT_MASK.SCALE_ENABLED) {
            mSGDetector.onTouchEvent(event);
        }

        return mSGDetector.isInProgress() || mGDetector.onTouchEvent(event);
    }

    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------

    @Override
    public void onInit()
    {
        super.onInit();
    }


    @Override
    public void onShutdown()
    {
        super.onShutdown();

        mSGDetector = null;
        mGDetector = null;
    }

    @Override
    public boolean onRegisterInputListeners()
    {
        final Handler h = new Handler(mGame.getApplication().getMainLooper());

        // gesture detectors
        mSGDetector = new ScaleGestureDetector(mGame.getApplication(), this, h);

        mGDetector = new GestureDetector(mGame.getApplication(), this, h);
        mGDetector.setIsLongpressEnabled(false); // longpress not useful for games
        mGDetector.setOnDoubleTapListener(this);

        return true;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
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
    public boolean onScale(ScaleGestureDetector detector)
    {

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

    public boolean onScaleBegin(ScaleGestureDetector detector)
    {
        return mGame.getSettings().INPUT_EVENT_MASK.SCALE_ENABLED;
    }

    public void onScaleEnd(ScaleGestureDetector detector)
    {
        mScaleInProgress = false;
    }

    /**
     * Override to handle pointer down. Default implementation creates event
     * {@link ITouchEvent}.
     */
    public boolean onDown(MotionEvent e)
    {
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
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
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

    public void onLongPress(MotionEvent e)
    {
        // long press not enabled
    }

    /**
     * Override to handle scroll gestures. Default implementation creates event
     * {@link IScrollEvent}.
     */
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY)
    {
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
    public void onShowPress(MotionEvent e)
    {
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
    public boolean onSingleTapUp(MotionEvent e)
    {
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
    public boolean onDoubleTap(MotionEvent e)
    {
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

    public boolean onDoubleTapEvent(MotionEvent e)
    {
        return false;
    }

    /**
     * Override to handle single tap confirmed. Default implementation creates event
     * {@link ITouchEvent}.
     */
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
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


}
