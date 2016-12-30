package stachelsau.snare.events;

import android.view.GestureDetector;
import android.view.MotionEvent;


public interface IScrollEvent extends IEvent {
    public static final SystemEventType EVENT_TYPE = SystemEventType.UI_SCROLL;
    
    /** X coordinate as delivered by {@link MotionEvent} */
    int getTouchX();
    /** Inverted y coordinate (screenHeight - {@link MotionEvent}.getY()) */
    int getTouchY();
    /** Distance as delivered by {@link GestureDetector}*/
    float getDistanceX();
    /** Distance as delivered by {@link GestureDetector}*/
    float getDistanceY(); 
    void setScrollData(int x, int y, float distanceX, float distanceY);
}
