package com.moonymango.snareTest;


import android.support.test.runner.AndroidJUnit4;

import com.moonymango.snare.util.RingBuffer;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class RingBufferTest {
    
    private RingBuffer<String> mRB;
    private final String s1 = "string 1";
    private final String s2 = "string 2";
    private final String s3 = "string 3";
    private final String s4 = "string 4";

    @Test
    public void testFunction() {
        mRB = new RingBuffer<>(3);
        mRB.add(s1);
        mRB.add(s2);
        mRB.add(s3);
        mRB.add(s4); // this shall overwrite s1
        
        String s = mRB.get(0);
        assertEquals(s4, s);  // last added elem must be on index 0
        
        s = mRB.get(1);
        assertEquals(s3, s);  // elem before on index 1
        
        s = mRB.get(2);
        assertEquals(s2, s);  // elem before that on index 2
        
        mRB.clear();
        s = mRB.get(0);
        assertTrue(s == null);
        s = mRB.get(1);
        assertTrue(s == null);
        s = mRB.get(2);
        assertTrue(s == null);
        
    }

    @Test
    public void testDefault() {
        mRB = new RingBuffer<>(3, s4);
        String s = mRB.get(0);
        assertEquals(s4, s);  // nothing added, so default result expected
        
        mRB.add(s1);
        s = mRB.get(1);
        assertEquals(s4, s);  
    }
    
}
