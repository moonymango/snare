package stachelsau.snare.events;

import stachelsau.snare.events.EventManager.IEventType;

/**
 * Base interface that every other event interface has to inherit from.
 */
public interface IEvent {

    void recycle();
    IEventType getType();
    
    static enum SystemEventType implements IEventType {
        INVALID,
        
        STATS_UPDATE,       // event to distribute FPS data
        GAME_STATE_CHANGED, // new game state has become active
        
        GAMEOBJ_TOUCH,      // game obj representation on screen touched by user
        GAMEOBJ_NEW,        // announce newly created game obj
        GAMEOBJ_DESTROY,    // announce destruction of game obj  
        GAMEOBJ_MOVE,       // game obj has moved
        GAMEOBJ_SCALE,      // game obj scaled
        GAMEOBJ_ROTATE,     // game obj rotated
        GAMEOBJ_COLLISION,  // collision between 2 obj
        
        UI_TOUCH,           // user has touched the screen
        UI_FLING,           // fling gesture
        UI_SCROLL,          // scroll gesture
        UI_SCALE,           // scale gesture
        UI_KEY,             // key input (e.g. back button)
        UI_WIDGET_TOUCHED_BEGIN,  // widget was touched, visible feedback started
        // note: in case the widget has no visual feedback, then there will only
        // the UI_WIDGET_TOUCHED_END event
        UI_WIDGET_TOUCHED_END,  // widget was touched, visible feedback has ended
        UI_CAMERA_MOVE,     // camera has moved
        
        USER_EVENT;         // general purpose event
    
        public String getName() {
            return this.name();
        }   
    }
    
}
