package com.moonymango.snare.util;

import android.graphics.Color;

public class HSVColor {

    private static final float[] sColor = new float[4];
    private static final float[] sIn = new float[3];
    
    public static float[] toRGB(float[] hsv, float alpha) {
        int c = Color.HSVToColor(hsv);
        //(alpha << 24) | (red << 16) | (green << 8) | blue
        sColor[0] = ((float) ((c & 0x00ff0000) >> 16)) / 255;
        sColor[1] = ((float) ((c & 0x0000ff00) >> 8)) / 255;
        sColor[2] = ((float) ((c & 0x000000ff))) / 255;
        sColor[3] = alpha; // ((float) ((c & 0xff000000) >> 24)) / 255;;
        return sColor;    
    }
    
    public static float[] toRGB(float h, float s, float v, float alpha) {
        sIn[0] = h;
        sIn[1] = s;
        sIn[2] = v;
        return toRGB(sIn, alpha);
    }
    
}
