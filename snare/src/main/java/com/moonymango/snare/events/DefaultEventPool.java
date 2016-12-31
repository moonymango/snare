package com.moonymango.snare.events;

import com.moonymango.snare.util.Pool;

public class DefaultEventPool extends Pool<DefaultEvent> {

    @Override
    protected DefaultEvent allocatePoolItem() {
        return new DefaultEvent();
    }

}
