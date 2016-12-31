package com.moonymango.snareDemo.touch;

import com.moonymango.snare.util.Pool;

public class DemoEventPool extends Pool<DemoEvent> {

    @Override
    protected DemoEvent allocatePoolItem() {
        return new DemoEvent();
    }

}
