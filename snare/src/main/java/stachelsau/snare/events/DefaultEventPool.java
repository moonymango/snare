package stachelsau.snare.events;

import stachelsau.snare.util.Pool;

public class DefaultEventPool extends Pool<DefaultEvent> {

    @Override
    protected DefaultEvent allocatePoolItem() {
        return new DefaultEvent();
    }

}
