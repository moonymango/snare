package com.moonymango.snare.util;

public class RingBuffer<T> {
    
    private final int mSize;
    private final Object[] mBuffer;
    private int mPointer;
    private final T mDefault;

    public RingBuffer(int size, T def) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must not be less than 1.");
        }
        mSize = size;
        mBuffer = new Object[size];
        mDefault = def;
    }
    
    public RingBuffer(int size) {
        this(size, null);
    }
    
    public void add(T elem) {
        if (elem == null) {
            return;
        }
        mBuffer[mPointer] = elem;
        mPointer = (mPointer + 1) % mSize;
    }
    
    @SuppressWarnings("unchecked")
    public T get(int idx) {
        if (idx < 0 || idx > mSize-1) {
            throw new IllegalArgumentException("Invalid index.");
        }
        final int pos = (mPointer + mSize - idx - 1) % mSize;
        // cast is safe because we added only T to mBuffer
        T result = (T) mBuffer[pos];
        if (result == null) {
            result = mDefault;
        }
        return result;
    }
    
    public void clear() {
        for (int i = 0; i < mSize; i++) {
            mBuffer[i] = null;
        }
        mPointer = 0;
    }
    
    public int getSize() {
        return mSize;
    }
}
