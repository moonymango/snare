package com.moonymango.snare.audio;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjTouchEvent;
import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.ui.TouchAction;

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
        SnareGame.get().getEventManager().addListener(IGameObjTouchEvent.EVENT_TYPE, this);
    }

    @Override
    public void onShutdown() {
        super.onShutdown();
        SnareGame.get().getEventManager().removeListener(IGameObjTouchEvent.EVENT_TYPE, this);
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
