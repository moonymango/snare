package com.moonymango.snare.ui;

import android.graphics.Color;

import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

import java.util.ArrayList;

/**
 * Wrapper that provides some runtime color manipulation.
 * Other classes may implement {@link IColorSeqListener} and register to the ColorWrapper.
 */
public class ColorWrapper extends BaseProcess
{
    private ArrayList<IColorSeqListener> mListeners = new ArrayList<>();

    /** Native color which is base for all following color modifications. */
    private final float[] mNativeColor = {0, 0, 0, 1};
    /** Color which is result of all color modifications. */
    private final float[] mActualColor = new float[4];
    /** Enable flag for inversion. */
    private boolean mIsInverted;
    /** Enable flag for conversion to greyscale. */
    private boolean mIsGreyscale;

    /** Color for temporary overlay. */
    private final float[] mOverlayColor = new float[4];
    private boolean mHasOverlay;
    /** Parameters for Overlay fading. */
    private float mTimeScale;
    private float mLocalTime;
    private ClockType mClockType;


    public ColorWrapper()
    {
    }

    public ColorWrapper(float[] hsv, float a)
    {
        setColorHSV(hsv, a);
    }

    public ColorWrapper(float[] rgba)
    {
        setColor(rgba);
    }

    public ColorWrapper(ColorWrapper other)
    {
        System.arraycopy(other.mNativeColor, 0, mNativeColor, 0, 4);
        System.arraycopy(other.mOverlayColor, 0, mOverlayColor, 0, 4);
        mHasOverlay = other.mHasOverlay;
        mIsInverted = other.mIsInverted;
        mIsGreyscale = other.mIsGreyscale;
    }

    @Override
    protected void onInit()
    {
        for (int l = mListeners.size() - 1; l >= 0; l--)
        {
            mListeners.get(l).onColorChange(this);
        }
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta, float virtualDelta)
    {
        if (mClockType == null)
        {
            return false;
        }

        final float delta = mClockType == ClockType.REALTIME ? realDelta : virtualDelta;
        mLocalTime += delta * mTimeScale;
        boolean running = true;
        if (mLocalTime > 1.0f)
        {
            // overlay has been vanished, stop process
            mLocalTime = 1.0f;
            running = false;
        }
        updateColor();
        mHasOverlay = running;
        return running;
    }


    @Override
    protected void onKill()
    {
    }

    @Override
    public void run()
    {
        throw new UnsupportedOperationException("Direct call to run not supported.");
    }

    /**
     * Enables color overlay.
     * @param overlayColor Color to overlay.
     * @return this
     */
    public ColorWrapper enableOverlay(float[] overlayColor)
    {
        System.arraycopy(overlayColor, 0, mOverlayColor, 0, 4);
        mHasOverlay = true;
        mLocalTime = 0;
        updateColor();
        kill();  // stop any ongoing overlay transition
        return this;
    }

    /**
     * Disables color overlay immediately.
     * @return this
     */
    public ColorWrapper disableOverlay()
    {
        if (!mHasOverlay) throw new IllegalStateException("color overlay was not enabled.");
        mHasOverlay = false;
        kill();
        updateColor();
        return this;
    }

    /**
     * Starts fading of overlay color.
     * @param millis  Duration until overlay is faded completely.
     * @param clock Clock base to use.
     * @return
     */
    public ColorWrapper disableOverlay(int millis, ClockType clock)
    {
        if (!mHasOverlay) throw new IllegalStateException("color overlay was not enabled.");
        mClockType = clock;
        mTimeScale = 1.0f / millis;
        super.run();
        return this;
    }

    public ColorWrapper addListener(IColorSeqListener listener)
    {
        mListeners.add(listener);
        listener.onColorChange(this);
        return this;
    }

    public ColorWrapper removeListener(IColorSeqListener listener)
    {
        mListeners.remove(listener);
        return this;
    }


    public ColorWrapper invert(boolean enable)
    {
        mIsInverted = enable;
        updateColor();
        return this;
    }

    public ColorWrapper greyscale(boolean enable)
    {
        mIsGreyscale = enable;
        updateColor();
        return this;
    }

    public ColorWrapper setColor(float r, float g, float b, float a)
    {
        mNativeColor[0] = r;
        mNativeColor[1] = g;
        mNativeColor[2] = b;
        mNativeColor[3] = a;
        updateColor();
        return this;
    }

    public ColorWrapper setColor(float[] rgba)
    {
        for (int i = 0; i < 4; i++)
        {
            mNativeColor[i] = rgba[i];
        }
        updateColor();
        return this;
    }

    /**
     * Sets color via HSV components.
     * H [0..360], S [0..1], V [0..1], alpha [0..1]
     *
     * @param hsv HSV color component.
     * @param a  Alpha
     * @return this
     */
    public ColorWrapper setColorHSV(float[] hsv, float a)
    {
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
     *
     * @param h H component
     * @param s S component
     * @param v V component
     * @param a Alpha
     * @return this
     */
    public ColorWrapper setColorHSV(float h, float s, float v, float a)
    {
        mNativeColor[0] = h;
        mNativeColor[1] = s;
        mNativeColor[2] = v;
        setColorHSV(mNativeColor, a);
        return this;
    }

    public float[] getActualColor()
    {
        return mActualColor;
    }

    public void copyActualColor(float[] target)
    {
        System.arraycopy(mActualColor, 0, target, 0, 4);
    }

    private void updateColor()
    {

        if (mHasOverlay)
        {
            for (int i = 0; i < 4; i++)
                mActualColor[i] = mNativeColor[i]*mLocalTime + mOverlayColor[i]*(1-mLocalTime);
        }
        else
        {
            System.arraycopy(mNativeColor, 0, mActualColor, 0 , 4);
        }

        // inversion
        if (mIsInverted)
        {
            for (int i = 0; i < 3; i++)
            {
                // do not apply inversion to alpha component
                mActualColor[i] = 1 - mActualColor[i];
            }
        }

        // greyscale conversion
        if (mIsGreyscale)
        {
            final float y = 0.2126f * mActualColor[0] +
                            0.7152f * mActualColor[1] +
                            0.0722f * mActualColor[2];
            for (int i = 0; i < 3; i++)
            {
                mActualColor[i] = y;
            }
        }

        // notify listeners
        for (int l = mListeners.size() - 1; l >= 0; l--)
        {
            mListeners.get(l).onColorChange(this);
        }
    }

    public interface IColorSeqListener
    {
        /**
         * Called when new color available
         */
        void onColorChange(ColorWrapper cp);
    }

}
