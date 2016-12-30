package stachelsau.snare.ui.widgets;

import java.util.ArrayList;

import stachelsau.snare.events.ITouchEvent;
import stachelsau.snare.ui.IScreenElement;
import stachelsau.snare.ui.PlayerGameView;

/**
 * Virtual screen with an indefinite size. The position of visible area on that 
 * virtual screen defines what widgets are actually drawn. The dimensions of the
 * visible area is the same as physical screen dimensions. The order of drawing 
 * and touch handling is defined as follows: 
 *   1. widgets are are drawn in order they were added
 *   2. touch events are are handled in reverse order in which widgets were added
 * Note: {@link VirtualScreen} manages the position of its widgets, so those
 * widget's position must not be manipulated directly!   
 */
public class VirtualScreen implements IScreenElement, IPositionable2D {
    
    final ArrayList<WidgetWrapper> mWidgets = new ArrayList<WidgetWrapper>();
    
    private PlayerGameView mView;
    private int mLeft;
    private int mRight;
    private int mTop;
    private int mBottom;
    private boolean mIsVisible = true;

    @Override
    public void draw() {
        final int len = mWidgets.size();
        for (int i = 0; i < len; i++) {
            final WidgetWrapper ww = mWidgets.get(i); 
            if (ww.mInVisibleArea && ww.mWidget.isVisible()) {
                ww.mWidget.draw();
            }
        }
    }

    @Override
    public void onAttachToScreen(PlayerGameView view, int screenWidth,
            int screenHeight) {
        final int len = mWidgets.size();
        for (int i = 0; i < len; i++) {
            mWidgets.get(i).mWidget.onAttachToScreen(view, screenWidth, 
                    screenHeight);
        }
        mView = view;
        updateBoundingRect();
    }

    @Override
    public void onDetachFromScreen() {
        final int len = mWidgets.size();
        for (int i = 0; i < len; i++) {
            mWidgets.get(i).mWidget.onDetachFromScreen();
        }
        mView = null;
        
    }

    @Override
    public void show(boolean visible) {
        /* Note: when visibility is enabled, widgets may still
         * be invisible due to their own visibility property */
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
        for (int i = mWidgets.size()-1; i >= 0; i--) {
            final WidgetWrapper ww = mWidgets.get(i); 
            if (ww.mInVisibleArea && ww.mWidget.isVisible() && 
                    ww.mWidget.onTouchEvent(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsCoord(int x, int y) {
        // virtual screen has indefinite dimensions
        return true;
    }
    
    /**
     * Add a widget to the area. The bounding box of the widget is
     * evaluated only once, so later manipulation on the widget 
     * (angle, scale) will NOT be reflected, which may lead to incorrect
     * visibility test results. 
     * TODO prioD: remove this limitation  
     * You are safe as long as you change nothing that requires the 
     * area's bounding box to be expanded.
     * Note: this method performs allocations.
     */
    public VirtualScreen addWidget(BaseWidget w) {
        final WidgetWrapper ww = new WidgetWrapper(w);
        mWidgets.add(ww);
        
        if (mView != null) {
            w.onAttachToScreen(mView, mView.getScreenWidth(), 
                    mView.getScreenHeight());
        }
        updateWidget(ww);
        return this;
    }
    
    /** 
     * Sets position of visible area's origin (lower left corner).
     * The dimensions of visible area is the same as physical screen.
     * So the visible area will be a rectangle ranging from lower
     * left (x, y) to upper right (x + screenWidth, y + screenHeight).
     * @param x
     * @param y
     */
    public VirtualScreen setPosition(int x, int y) {
        mLeft = x;
        mBottom = y;
        updateBoundingRect();
        return this;
    }
    
    public int getPositionX() {return mLeft;}
    public int getPositionY() {return mBottom;}

    private void updateWidget(WidgetWrapper ww) {
        // check overlap with visible area, position update only
        // necessary if widget was or is in visible area
        final boolean before = ww.mInVisibleArea;
        ww.mInVisibleArea = !(mLeft > ww.mRight || mRight < ww.mLeft 
                || mTop < ww.mBottom || mBottom > ww.mTop);
        if (before || ww.mInVisibleArea) {
            ww.mWidget.setPosition(ww.mX - mLeft, ww.mY - mBottom);
        }
    }
    
    private void updateBoundingRect() {
        if (mView == null) {
            return;
        }
        mRight = mLeft + mView.getScreenWidth();
        mTop = mBottom + mView.getScreenHeight();
        for (int i = mWidgets.size()-1; i >= 0; i--) {
            updateWidget(mWidgets.get(i));
        }
    }
                     
    /**
     * Wrapper which holds reference to widget and initial values.
     * (Values of widget itself will be changed based on viewing position
     *  on virtual screen.)
     */
    private static class WidgetWrapper {
        private final BaseWidget mWidget;
        private final int mX;
        private final int mY;
        private final int mLeft;
        private final int mRight;
        private final int mTop;
        private final int mBottom;
        private boolean mInVisibleArea;
        
        private WidgetWrapper(BaseWidget w) {
            mWidget = w;
            mX = w.getPositionX();
            mY = w.getPositionY();
            mLeft = w.getAABBLeft();
            mRight = w.getAABBRight();
            mTop = w.getAABBTop();
            mBottom = w.getAABBBottom();
        }
    }

}
