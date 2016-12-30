package stachelsau.snare.game.logic;

import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.IScrollEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.ComponentType;

public class ScrollLogicComponent extends BaseComponent implements
        IEventListener {

    private final IScrollHandler mHandler;
    
    public ScrollLogicComponent(IScrollHandler handler) {
        super(ComponentType.LOGIC);
        mHandler = handler;
    }
    
    @Override
    public void onInit() {
        Game.get().getEventManager().addListener(IScrollEvent.EVENT_TYPE, this);
    }

    @Override
    public void onShutdown() {
        Game.get().getEventManager().removeListener(IScrollEvent.EVENT_TYPE, this);
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
