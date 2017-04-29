package com.moonymango.snareDemo.touch;

import com.moonymango.snare.events.DefaultEvent;
import com.moonymango.snare.game.IGame;

public class DemoEvent extends DefaultEvent implements IDemoObjCatchedEvent {
    public DemoEvent(IGame game)
    {
        super(game);
    }
}
