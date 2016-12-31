package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.IOnProcessKilledListener;
import com.moonymango.snare.proc.ProcessManager.ProcState;
import com.moonymango.snare.ui.IScreenElement;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.util.EasingProfile;

/**
 * Moves a widget based on real time ( {@link ClockType}).
 */
public class WidgetMotionModifier extends BaseProcess {
    
    private MotionSettings mS = new MotionSettings();
    private int mDeltaX;
    private int mDeltaY;
    
    private float mTime;
    
    /**
     * Constructs an unconfigured modifier. 
     */
    public WidgetMotionModifier() {}
    
    /**
     * Constructs and configures modifier. The instance doesn't keep a reference
     * to settings so it may be used to configure other modifiers as well.
     * @param settings
     */
    public WidgetMotionModifier(MotionSettings settings) {
        configure(settings);
    }
    
    /**
     * Configures the modifier. The instance doesn't keep a reference
     * to settings so it may be used to configure other modifiers as well.
     * @param settings
     * @return this
     */
    public WidgetMotionModifier configure(MotionSettings settings) {
        if (getState() != ProcState.DEAD) {
            throw new IllegalStateException("Cannot configure running process.");
        }
        mS.copy(settings);
        setListener(settings.listener);
        setDelay(ClockType.REALTIME, settings.delay);
        return this;
    }
    
    @Override
    protected void onInit() {
        if (mS.widget == null || mS.profile == null) {
            throw new IllegalStateException("Cannot run process without configuration.");
        }
        if (mS.startFromCurrentPos) {
            mS.startX = mS.widget.getPositionX();
            mS.startY = mS.widget.getPositionY();
        }
        mS.widget.setPosition(mS.startX, mS.startY);
        
        mDeltaX = mS.targetX - mS.startX;
        mDeltaY = mS.targetY - mS.startY;
        mTime = 0;
        
        if (mS.addToViewBeforeMotion && mS.view != null)  {
            final IScreenElement widget = (IScreenElement) mS.widget;
            if (!widget.isAttached()) {
                mS.view.pushScreenElement(widget);
            }
        }
    }
    
    @Override
    protected void onKill() {
        mS.widget.setPosition(mS.targetX, mS.targetY);
        if (mS.removeFromViewAfterMotion) {
            final IScreenElement widget = (IScreenElement) mS.widget;
            final PlayerGameView view = widget.getView();
            if (view != null) {
                view.removeScreenElement(widget);
            }
        }
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        mTime += realDelta;
        if (mTime >= mS.duration) {
            mS.widget.setPosition(mS.targetX, mS.targetY);
            return false;
        }
        
        final float t = mS.profile.value(mTime/mS.duration);
        final int x = (int) (mS.startX + mDeltaX*t);
        final int y = (int) (mS.startY + mDeltaY*t);
        mS.widget.setPosition(x, y);
        return true;
    }
    
    /** 
     * Helper class to define the motion. The {@link WidgetMotionModifier} 
     * doesn't keep a reference to this, so a single instance can be used to
     * configure multiple modifiers.
     */
    public static class MotionSettings implements Cloneable {
        /** Widget to move */
        public IPositionable2D widget;
        /** Easing function used for motion (Default = LINEAR) */
        public EasingProfile profile = EasingProfile.LINEAR;
        /** 
         * Duration of motion in milliseconds. This will define the speed.
         * Default = 500 ms.
         * Note: {@link ClockType} real time is always used.  
         */
        public float duration = 500;
        /**
         * Delay until motion starts after scheduling the process.
         * Uses the realtime {@link ClockType}.
         * Default = 0 ms;
         */
        public float delay = 0;
        /** 
         * Use the widget's current position as start coordinates for motion 
         * (Default = true)
         */
        public boolean startFromCurrentPos = true;
        /** 
         * Explicit start coordinate. Not effective when startFromCurrentPos
         * is set to true.
         */
        public int startX;
        /** 
         * Explicit start coordinate. Not effective when startFromCurrentPos
         * is set to true.
         */
        public int startY;
        /** Motion target coordinate. */
        public int targetX;
        /** Motion target coordinate. */
        public int targetY;
        /** IOnProcessKilledListener to be notified when motion is finished. */
        public IOnProcessKilledListener listener; 
        /** 
         * Add widget to {@link PlayerGameView} before motion. 
         * (default = false)
         */
        public boolean addToViewBeforeMotion = false;
        /** View to add widget to. (default = game primary view) */
        public PlayerGameView view = Game.get().getPrimaryView();
        /** 
         * Remove widget from its view after motion is finished. 
         * default = false 
         */
        public boolean removeFromViewAfterMotion = false;
        
        public void copy(MotionSettings other) {
            addToViewBeforeMotion = other.addToViewBeforeMotion;
            duration = other.duration;
            listener = other.listener;
            profile = other.profile;
            removeFromViewAfterMotion = other.removeFromViewAfterMotion;
            startFromCurrentPos = other.startFromCurrentPos;
            startX = other.startX;
            startY = other.startY;
            targetX = other.targetX;
            targetY = other.targetY;
            view = other.view;
            widget = other.widget;
        }
    }

}
