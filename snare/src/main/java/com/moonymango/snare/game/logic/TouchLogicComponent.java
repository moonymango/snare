package com.moonymango.snare.game.logic;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjTouchEvent;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.ui.TouchAction;

public class TouchLogicComponent extends BaseComponent implements IEventListener {
   
    private ITouchInputHandler mHandler;
    
    public TouchLogicComponent(IGame game, ITouchInputHandler handler) {
        super(game, ComponentType.LOGIC);
        mHandler = handler;
    }

    @Override
    public void onInit() {
        mGame.getEventManager().addListener(IGameObjTouchEvent.EVENT_TYPE, this);
        
    }

    @Override
    public void onShutdown() {
        mGame.getEventManager().removeListener(IGameObjTouchEvent.EVENT_TYPE, this);
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
