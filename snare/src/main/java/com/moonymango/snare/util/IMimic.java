package com.moonymango.snare.util;

public interface IMimic<T> {
    /** 
     * Adopts data from an original.
     * @return True in case of success, false otherwise. 
     */
    boolean mimic(T original);
}
