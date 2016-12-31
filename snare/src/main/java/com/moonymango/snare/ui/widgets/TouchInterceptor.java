package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.ui.IScreenElement;
import com.moonymango.snare.ui.PlayerGameView;

/**
 * Screen element that draws nothing but may be used to intercept touch
 * events as they travel down the {@link PlayerGameView}'s screen element stack.
 * Intended use is to filter or block touch events before they reach lower
 * screen elements.
 */
public class TouchInterceptor implements IScreenElement {

    private final IOnTouchInterceptListener mListener;
    private final boolean mFullScreen;
    private final int mMinX;
    private final int mMaxX;
    private final int mMinY;
    private final int mMaxY;
    private PlayerGameView mView;
    private boolean mIsVisible;
    
    /**
     * Constructs interceptor which covers the full screen. If no listener
     * is provided, then all touch events will be blocked and discarded.
     * @param l Listener. 
     */
    public TouchInterceptor(IOnTouchInterceptListener l) {
        mListener = l;
        mFullScreen = true;
        mMaxX = 0;
        mMaxY = 0;
        mMinX = 0;
        mMinY = 0;
    }
    
    /**
     * Constructs interceptor which covers specified area. If no listener 
     * is provided, then all touch events will be blocked and discarded.
     * @param l
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    public TouchInterceptor(IOnTouchInterceptListener l, int minX, int maxX, 
            int minY, int maxY) {
        mListener = l; 
        mFullScreen = false;
        mMaxX = maxX;
        mMaxY = maxY;
        mMinX = minX;
        mMinY = minY;
    }
    
    /**
     * Enables or disables interception.
     * @param yesNo True to intercept, false to make all touch events pass
     *              through.
     */
    public void enable(boolean yesNo) {
        mIsVisible = yesNo;
    }
    
    @Override
    public void draw() {}

    @Override
    public void onAttachToScreen(PlayerGameView view, int screenWidth,
            int screenHeight) {
        mView = view;
    }

    @Override
    public void onDetachFromScreen() {
        mView = null;
    }

    @Override
    public void show(boolean visible) {
        mIsVisible = visible;
    }

    @Override
    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public boolean isAttached() {
        return mView != null;
    }

    @Override
    public PlayerGameView getView() {
        return mView;
    }

    @Override
    public boolean onTouchEvent(ITouchEvent e) {
        if (mListener == null) {
            return true;
        }
        return mListener.onIntercept(this, e);
    }

    @Override
    public boolean containsCoord(int x, int y) {
        if (mFullScreen) {
            return true;
        }
        return x >= mMinX && x <= mMaxX && y >= mMinY && y <= mMaxY;
    }

    public interface IOnTouchInterceptListener {
        /** 
         * Handles intercepted touch event.
         * 
         * @param i
         * @param e
         * @return True if event was intercepted and should not be passed to
         *          further screen elements, false otherwise.
         */
        boolean onIntercept(TouchInterceptor i, ITouchEvent e);
    }
}
