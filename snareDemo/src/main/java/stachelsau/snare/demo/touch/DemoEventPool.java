package stachelsau.snare.demo.touch;

import stachelsau.snare.util.Pool;

public class DemoEventPool extends Pool<DemoEvent> {

    @Override
    protected DemoEvent allocatePoolItem() {
        return new DemoEvent();
    }

}
