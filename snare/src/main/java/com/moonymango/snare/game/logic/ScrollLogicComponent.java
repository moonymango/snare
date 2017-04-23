package com.moonymango.snare.game.logic;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IScrollEvent;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;

public class ScrollLogicComponent extends BaseComponent implements
        IEventListener {

    private final IScrollHandler mHandler;
    
    public ScrollLogicComponent(IGame game, IScrollHandler handler)
    {
        super(game, ComponentType.LOGIC);
        mHandler = handler;
    }
    
    @Override
    public void onInit() {
        SnareGame.get().getEventManager().addListener(IScrollEvent.EVENT_TYPE, this);
    }

    @Override
    public void onShutdown() {
        SnareGame.get().getEventManager().removeListener(IScrollEvent.EVENT_TYPE, this);
    }

    public boolean handleEvent(IEvent event) {
        IScrollEvent e = (IScrollEvent) event;
        mHandler.handleScroll(getGameObj(), e.getTouchX(), e.getTouchY(), 
                e.getDistanceX(), e.getDistanceY());
        return false;
    }

    public interface IScrollHandler {
        void handleScroll(GameObj obj, int x, int y, float distanceX, float distanceY);
    }
}
