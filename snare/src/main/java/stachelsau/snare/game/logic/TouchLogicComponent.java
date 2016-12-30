package stachelsau.snare.game.logic;

import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.IGameObjTouchEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.ComponentType;
import stachelsau.snare.ui.TouchAction;

public class TouchLogicComponent extends BaseComponent implements IEventListener {
   
    private ITouchInputHandler mHandler;
    
    public TouchLogicComponent(ITouchInputHandler handler) {
        super(ComponentType.LOGIC);
        mHandler = handler;
    }

    @Override
    public void onInit() {
        Game.get().getEventManager().addListener(IGameObjTouchEvent.EVENT_TYPE, this);
        
    }

    @Override
    public void onShutdown() {
        Game.get().getEventManager().removeListener(IGameObjTouchEvent.EVENT_TYPE, this);
    }
    
    public boolean handleEvent(IEvent event) {
        final IGameObjTouchEvent e = (IGameObjTouchEvent) event;
        final GameObj obj = getGameObj();
        final int id = obj.getID();
        if (e.getGameObjID() == id) {
            mHandler.handleTouch(obj, e.getTouchAction());
        }
        return false; 
    }
    
    public interface ITouchInputHandler {
        /**
         * Handles touch.
         * @param action
         * @return True, if touch was handled, else false. 
         */
        void handleTouch(GameObj obj, TouchAction action);
    }
}
