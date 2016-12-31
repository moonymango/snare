package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.IOnProcessKilledListener;

public class WidgetScaleModifier extends BaseProcess {

    private BaseWidget mWidget;
    private long mDuration;
    private float mScaleX;
    private float mScaleY;
    private float mCoeffX;
    private float mCoeffY;
    private float mStartScaleY;
    private float mStartScaleX;
    private float mProgress;
 
    public WidgetScaleModifier() {
            
    }
    
    public WidgetScaleModifier(BaseWidget widget, float scaleX, float scaleY, 
            long milliseconds, IOnProcessKilledListener listener) {
        configure(widget, scaleX, scaleY, milliseconds, listener);
    }
    
    public WidgetScaleModifier configure(BaseWidget widget, float scaleX, float scaleY, 
            long duration, IOnProcessKilledListener listener) {  
        mWidget = widget;
        mDuration = duration;
        mScaleX = scaleX;
        mScaleY = scaleY;
        setListener(listener);
        return this;
    }
    
    @Override
    public void onInit() {
        if (mWidget == null) {
            throw new IllegalStateException("Cannot run process without configuration.");
        }
        mStartScaleX = mWidget.getScaleX();
        mStartScaleY = mWidget.getScaleY();
        final float finalScaleX = mStartScaleX * mScaleX;
        final float finalScaleY = mStartScaleY * mScaleY;
        mCoeffX = (finalScaleX - mStartScaleX) / mDuration;
        mCoeffY = (finalScaleY - mStartScaleY) / mDuration;
        mProgress = 0;
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        mProgress += realDelta;
        if (mProgress > mDuration) {
            return false;
        }
        final float x = mStartScaleX + mCoeffX*mProgress;
        final float y = mStartScaleY + mCoeffY*mProgress;
        mWidget.setScale(x, y);
        return true;
    }

    @Override
    protected void onKill() {
        // make sure we leave with exact dimension regardless of the
        // last delta
        mWidget.setScale(mStartScaleX*mScaleX, mStartScaleY*mScaleY);
    }

}
