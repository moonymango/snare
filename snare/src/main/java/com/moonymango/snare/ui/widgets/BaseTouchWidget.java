package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.ITouchEvent;
import com.moonymango.snare.events.IWidgetTouchedBeginEvent;
import com.moonymango.snare.events.IWidgetTouchedEndEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.proc.ProcessManager.IOnProcessKilledListener;
import com.moonymango.snare.ui.TouchAction;

/**
 * Extends {@link BaseWidget} so it can act as button.
 */
public abstract class BaseTouchWidget extends BaseWidget implements
        IOnProcessKilledListener {

    private boolean mRunsClickAnimation;
    private BaseProcess mClickAnimation;
    private final TouchSetting mSetting;
    private boolean mIsEnabled = true;

    public BaseTouchWidget(TouchSetting setting) 
    {
    	mSetting = setting;
    	if (setting == TouchSetting.TOUCHABLE) {
    		setDefaultAnimation();    		
    	}
    }

    /**
     * Set a user defined animation for this button. Animation is played when
     * the button catches a touch DOWN event. {@link IOnButtonClickedListener}
     * is called after the animation process chain has finished.
     * 
     * @param proc
     *            Animation process or null if no animation desired.
     */
    public BaseTouchWidget setAnimation(BaseProcess proc) {
        if (proc != null) {
            proc.findLast().setListener(this);
        }
        mClickAnimation = proc;
        return this;
    }

    /**
     * Restores the default animation.
     */
    public BaseTouchWidget setDefaultAnimation() {
        WidgetScaleModifier min = new WidgetScaleModifier(this, 0.8f, 0.8f, 60,
                null);
        WidgetScaleModifier mag = new WidgetScaleModifier(this, 1.25f, 1.25f,
                60, null);
        min.setNext(mag);
        setAnimation(min);
        return this;
    }
    
    /**
     * @return True if widget responds to touch events, false otherwise.
     */
    public boolean isEnabled() {
        return mSetting == TouchSetting.TOUCHABLE && mIsEnabled;
    }
    
    /**
     * Enables/disables touch event handling.
     * @param enable
     * @return
     */
    public BaseTouchWidget enable(boolean enable) {
        mIsEnabled = enable;
        return this;
    }

    @Override
    public boolean onTouchEvent(ITouchEvent e) {
          
        if (mSetting != TouchSetting.TOUCHABLE || !mIsEnabled 
                || e.getTouchAction() != TouchAction.DOWN 
                || mRunsClickAnimation) {
            // Only do something if it's a DOWN action, we have a listener
            // and animation is not already running. Return true anyway, 
            // so that event is not delivered to another widget because it 
            // was actually meant for this one.
            return true;   
        }

        // play animation if we have one
        if (mClickAnimation != null) {
            mRunsClickAnimation = true;
            mClickAnimation.run();
            // send event 
        	final EventManager em = Game.get().getEventManager();
        	final IWidgetTouchedBeginEvent evt = em.obtain(IWidgetTouchedBeginEvent.EVENT_TYPE);
        	evt.setWidgetData(this);
        	em.queueEvent(evt);
        } else {
        	// no animation, send end event immediately
        	final EventManager em = Game.get().getEventManager();
        	final IWidgetTouchedEndEvent evt = em.obtain(IWidgetTouchedEndEvent.EVENT_TYPE);
        	evt.setWidgetData(this);
        	em.queueEvent(evt);        	
        } 
        
        return true;
    }
    
    @Override
    public void onProcessKilled(BaseProcess proc) {
        // gets called when animation is finished
        mRunsClickAnimation = false;
        
        // send end event
        final EventManager em = Game.get().getEventManager();
    	final IWidgetTouchedEndEvent evt = em.obtain(IWidgetTouchedEndEvent.EVENT_TYPE);
    	evt.setWidgetData(this);
    	em.queueEvent(evt);   
    }

    public enum TouchSetting {
    	TOUCHABLE,
    	NOT_TOUCHABLE
    }
}
