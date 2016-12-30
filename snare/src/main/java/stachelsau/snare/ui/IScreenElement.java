package stachelsau.snare.ui;

import stachelsau.snare.events.ITouchEvent;

public interface IScreenElement {

    void draw();
    
    /**
     * Called when screen element gets attached to the view or the view changes its dimensions.
     * @param view
     * @param screenWidth
     * @param screenHeight
     */
    void onAttachToScreen(PlayerGameView view, int screenWidth, int screenHeight);
    /**
     * Called when screen element is removed from the view.
     */
    void onDetachFromScreen();
    /** 
     * Sets visibility. Please note, that touch events are NOT delivered to
     * non-visible elements.
     * @param visible
     */
    void show(boolean visible);
    boolean isVisible();
    /** Indicates if element is attached to a view. */
    boolean isAttached();
    /** Returns view the element is attached to. */
    PlayerGameView getView();
    /**
     * Handle touch events.
     * @param e
     * @return True, if event was handled and should not be delivered to other screen elements.
     */
    boolean onTouchEvent(ITouchEvent e);
    /** Checks if element covers the specified screen coordinates. */
    boolean containsCoord(int x, int y);
    
}
