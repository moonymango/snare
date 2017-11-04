package com.moonymango.snare.ui;

import android.os.Handler;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.game.BaseGameView;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.ui.widgets.BaseTouchWidget;
import com.moonymango.snare.ui.widgets.BaseWidget;
import com.moonymango.snare.ui.widgets.Text;
import com.moonymango.snare.util.RingBuffer;

import java.util.ArrayList;

/**
 * Game view which handles screen output.
 */

public class PlayerGameView extends BaseGameView implements EventManager.IEventListener
{
    // ---------------------------------------------------------
    // static
    // ---------------------------------------------------------
    protected static final int DEBUG_PRINT_LINES = 40;
    protected final ArrayList<IScreenElement> mScreenElems = new ArrayList<IScreenElement>();
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    protected int mScreenWidth = 1;
    protected int mScreenHeight = 1;
    protected boolean mIsInitialized;
    protected Text mDebugText;
    protected RingBuffer<String> mDebugStrings;
    protected int mDebugPrintFontSize = 1;
    private float mScreenRatio = 1;
    private boolean mDebugStringsChanged;

    public PlayerGameView(IGame game)
    {
        super(game);
    }

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
            mDebugText = new Text(font, null, BaseWidget.PositionAlignment.LEFT_X_TOP_Y, BaseTouchWidget.TouchSetting.NOT_TOUCHABLE, 256);
            mDebugText.setTextSize(mDebugPrintFontSize);
            mDebugText.setColor(1, 1, 1, 0.4f);
            mDebugText.setPosition(0, mScreenHeight); // output in upper left corner
            mDebugText.onAttachToScreen(this, mScreenWidth, mScreenHeight);
        }

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

        mIsInitialized = false;
    }



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

    @Override
    public boolean onRegisterInputListeners()
    {
        return false; // no input capability
    }
}
