package com.moonymango.snare.util;

/**
 * Like a pair, just 3.
 */

public class Triple<F, S, T>
{

    public F first;
    public S second;
    public T third;

    public Triple() {}

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

}
