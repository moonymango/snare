package com.moonymango.snare.ui;

import java.lang.reflect.Field;

public final class InputEventMask {

    public boolean DOWN_ENABLED = false;
    public boolean SHOW_PRESS_ENABLED = false;
    public boolean DOUBLE_TAP_ENABLED = false;
    public boolean SINGLE_TAP_ENABLED = false;
    public boolean SINGLE_TAP_UP_ENABLED = false;
    
    public boolean FLING_ENABLED = false;
    public boolean SCROLL_ENABLED = false;
    public boolean SCALE_ENABLED = false;
    
    public void enableAll() {
        setValue(true);
    }
    
    public void disableAll() {
        setValue(false);
    }
    
    private void setValue(boolean val) {
        final Class<InputEventMask> clazz = InputEventMask.class;
        Field[] fields = clazz.getFields();
        
        for (int i = 0; i < fields.length; ++i) {
            try {
                fields[i].set(this, val);
            } catch (Exception e) {
                // will not happen because we hopefully have only boolean
                // fields in this class ;)
            }
        }
        
    }

}
