package com.moonymango.snareDemo.touch;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.util.Pool;

public class DemoEventPool extends Pool<DemoEvent> {

    public DemoEventPool(IGame game)
    {
        super(game);
    }

    @Override
    protected DemoEvent allocatePoolItem() {
        return new DemoEvent(mGame);
    }

}
