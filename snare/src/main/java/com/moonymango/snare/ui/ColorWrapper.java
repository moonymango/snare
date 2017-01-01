package com.moonymango.snare.ui;

import java.util.ArrayList;

import android.graphics.Color;

import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.ProcState;

/**
 * Wrapper that provides some runtime color manipulation. 
 * Other classes may implement {@link IColorSeqListener} and register to the
 * ColorWrapper.
 */
public class ColorWrapper extends BaseProcess {

    private ArrayList<IColorSeqListener> mListeners = 
            new ArrayList<IColorSeqListener>();
    
    private final ClockType mClockType;
    private final float[] mNativeColor = {0, 0, 0, 1};
    private final float[] mActualColor = new float[4];
    private boolean mIsInverted;
    private boolean mIsGreyscale;
    
    private float mTimeScale;
    private float mPos;
    private float[] mColorDiff = new float[4];
    
    private final float[] mOffsetDirection = new float[4];
    private float mOffset = 0;
    
    public ColorWrapper() {
        this(ClockType.REALTIME);
    }
    
    public ColorWrapper(ClockType clock) {
        mClockType = clock;
    }
    
    public ColorWrapper(float[] rgba) {
        this(ClockType.REALTIME);
        setColor(rgba);
    }
    
    public ColorWrapper(float[] rgba, ClockType clock) {
        mClockType = clock;
        setColor(rgba);
    }
    
    @Override
    protected void onInit() {  
        for (int l = mListeners.size()-1; l >= 0; l--) {
            mListeners.get(l).onColorChange(this);
        }
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
        final float delta = mClockType == ClockType.REALTIME ? realDelta :
            virtualDelta;
        mPos += delta * mTimeScale;
        boolean running = true;
        if (mPos > 1.0f) {
            mPos = 1.0f;
            running = false;
        }
        updateColor(); 
        return running;
    }
    
    public void run() {
        throw new UnsupportedOperationException("Use other run methods.");
    }
    
    /**
     * Changes color from specified start color back to native color.
     * @param millis Time to reach native color in milliseconds.
     * @param from start color
     */
    public void run(float millis, float[] from) {
        for (int i = 0; i < 4; i++) {
            mColorDiff[i] = mNativeColor[i] - from[i];
        }
        mTimeScale = 1.0f/millis;
        mPos = 0;
        super.run();
    }

    @Override
    protected void onKill() {}


    public ColorWrapper addListener(IColorSeqListener listener) {
        mListeners.add(listener);
        listener.onColorChange(this);
        return this;
    }
    
    public ColorWrapper removeListener(IColorSeqListener listener) {
        mListeners.remove(listener);
        return this;
    }
    
    /**
     * Offsets the palettes output color towards a target color. Amount is
     * a value in range [0..1], that defines how much the color should be
     * pulled towards the target.  
     * 
     * @param amount
     * @param r Target color red component.
     * @param g Target color green component.
     * @param b Target color blue component.
     * @param a Target color alpha component.
     */
    public ColorWrapper enableColorOffset(float amount, float r, float g, float b,
            float a) {
        mOffsetDirection[0] = r;
        mOffsetDirection[1] = g;
        mOffsetDirection[2] = b;
        mOffsetDirection[3] = a;
        mOffset = amount;
        updateColor();
        return this;
    }

    public ColorWrapper disableColorOffset() {
        mOffset = 0;
        updateColor();
        return this;
    }
    
    public ColorWrapper invert(boolean enable) {
        mIsInverted = enable;
        updateColor();
        return this;
    }
    
    public ColorWrapper greyscale(boolean enable) {
        mIsGreyscale = enable;
        updateColor();
        return this;
    }
    
    public ColorWrapper setColor(float r, float g, float b, float a) {
        mNativeColor[0] = r;
        mNativeColor[1] = g;
        mNativeColor[2] = b;
        mNativeColor[3] = a;
        updateColor();
        return this;
    }
    
    public ColorWrapper setColor(float[] rgba) {
        for (int i = 0; i < 4; i++) {
            mNativeColor[i] = rgba[i];
        }
        updateColor();
        return this;
    }
    
    /** 
     * Sets color via HSV components. 
     * H [0..360], S [0..1], V [0..1], alpha [0..1] 
     * @param hsv
     * @param a
     * @return
     */
    public ColorWrapper setColorHSV(float[] hsv, float a) {
        int c = Color.HSVToColor(hsv);
        //(alpha << 24) | (red << 16) | (green << 8) | blue
        mNativeColor[0] = ((float) ((c & 0x00ff0000) >> 16)) / 255;
        mNativeColor[1] = ((float) ((c & 0x0000ff00) >> 8)) / 255;
        mNativeColor[2] = ((float) ((c & 0x000000ff))) / 255;
        mNativeColor[3] = a;
        updateColor();
        return this;
    }
    
    /**
     * Sets color via HSV components. 
     * H [0..360], S [0..1], V [0..1], alpha [0..1]
     * @param h
     * @param s
     * @param v
     * @param a
     * @return
     */
    public ColorWrapper setColorHSV(float h, float s, float v, float a) {
        mNativeColor[0] = h;
        mNativeColor[1] = s;
        mNativeColor[2] = v;
        setColorHSV(mNativeColor, a);
        return this;
    }
    
    public float[] getActualColor()         {return mActualColor;}
    public void copyActualColor(float[] target) {
        for (int i = 0; i < 4; i++) {
            target[i] = mActualColor[i];
        }
    }
    
    private void updateColor() {
        
        if (getState() != ProcState.DEAD) {
            final float f = 1-mPos;
            for (int i = 0; i < 4; i++) {
                mActualColor[i] = mNativeColor[i] - f*mColorDiff[i]; 
            }
        } else {
            for (int i = 0; i < 4; i++) {
                mActualColor[i] = mNativeColor[i]; 
            }
        }
        
        // offset
        for (int i = 0; i < 4; i++) {
            final float d = mOffsetDirection[i] - mActualColor[i]; 
            mActualColor[i] = mActualColor[i] + d*mOffset;
        }
        
        // inversion
        if (mIsInverted) {
            for (int i = 0; i < 3; i++) {
                // do not apply inversion to alpha component
                mActualColor[i] = 1-mActualColor[i];
            }    
        }
        
        // greyscale conversion
        if (mIsGreyscale) {
            final float y  = 0.2126f * mActualColor[0] + 
                    0.7152f * mActualColor[1] + 
                    0.0722f * mActualColor[2];
            for (int i = 0; i < 3; i++) {
                mActualColor[i] = y;
            }
        }
        
        for (int l = mListeners.size()-1; l >= 0; l--) {
            mListeners.get(l).onColorChange(this);
        }
    }

    public interface IColorSeqListener {
        /** Called when new color available */
        void onColorChange(ColorWrapper cp);
    }
}