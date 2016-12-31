package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.IOnProcessKilledListener;
import com.moonymango.snare.proc.ProcessManager.ProcState;

/**
 * Rotate a widget based on real time clock ({@link ClockType}).
 */
public class WidgetRotationModifier extends BaseProcess {

    private BaseWidget mWidget;
    private float mAngularSpeed;
    private float mTargetAngle;
    
    private float mStartAngle;
    private float mAngle;
    
    public WidgetRotationModifier() {
     
    }
    
    public WidgetRotationModifier(BaseWidget widget, float angularSpeed, 
            float angle, IOnProcessKilledListener listener) {
        configure(widget, angularSpeed, angle, listener);
    }
    
    @Override
    protected void onInit() {
        if (mWidget == null) {
            throw new IllegalStateException("Cannot run process without configuration.");
        }
        mStartAngle = mWidget.getAngle();
        mAngle = 0;
        
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
        mAngle += mAngularSpeed * realDelta;
        if (mTargetAngle != 0 && mAngle > mTargetAngle) {
            return false;
        }
        
        mWidget.setAngle(mStartAngle + mAngle); 
        return true;
    }

    @Override
    protected void onKill() {
        // make sure we end up with exact angle setting, regardless
        // of delta times
        mWidget.setAngle(mStartAngle + mTargetAngle);
    }

    /**
     * Configure rotation.
     * @param widget
     * @param angularSpeed Degree per second.
     * @param angle Angle to rotate (0 = endless rotation)
     * @param listener
     */
    public WidgetRotationModifier configure(BaseWidget widget, float angularSpeed, float angle, 
            IOnProcessKilledListener listener) {
        if (getState() != ProcState.DEAD) {
            throw new IllegalStateException("Cannot configure running process.");
        }
        mWidget = widget;
        mAngularSpeed = angularSpeed/1000; // degrees per millisecond
        mTargetAngle = angle;
        setListener(listener);
        return this;
    }
}
