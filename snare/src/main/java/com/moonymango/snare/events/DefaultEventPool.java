package com.moonymango.snare.events;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.util.Pool;

public class DefaultEventPool extends Pool<DefaultEvent>
{
    public DefaultEventPool(IGame game)
    {
        super(game);
    }

    @Override
    protected DefaultEvent allocatePoolItem() {
        return new DefaultEvent(mGame);
    }

}
