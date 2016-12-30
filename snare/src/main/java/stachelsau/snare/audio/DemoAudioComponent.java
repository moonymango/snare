package stachelsau.snare.audio;

import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.IGameObjTouchEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.ui.TouchAction;

/**
 * This demo component plays a sound when its game object is being touched.
 */
public class DemoAudioComponent extends AudioComponent implements IEventListener {

    public DemoAudioComponent(SoundResource descr) {
        super(descr);
    }
    
    @Override
    public void onInit() {
        super.onInit();
        Game.get().getEventManager().addListener(IGameObjTouchEvent.EVENT_TYPE, this);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        Game.get().getEventManager().removeListener(IGameObjTouchEvent.EVENT_TYPE, this);
    }

    public boolean handleEvent(IEvent event) {
        final IGameObjTouchEvent e = (IGameObjTouchEvent) event;
        final int id = getGameObj().getID();
        if (e.getGameObjID() == id && e.getTouchAction() == TouchAction.DOWN) {
            play();
        }
        return false; 
    }

}
