package com.moonymango.snare.events;

import com.moonymango.snare.events.EventManager.IEventType;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.ui.TouchAction;
import com.moonymango.snare.ui.widgets.BaseTouchWidget;
import com.moonymango.snare.util.PoolItem;

/**
 * Base event that implements all of Snare's event interfaces. 
 * Derive from this to create a custom event classes.
 */
public class DefaultEvent extends PoolItem implements 
        ITouchEvent,
        IStatsUpdateEvent,
        IGameStateChangedEvent,
        IGameObjNewEvent,
        IGameObjDestroyEvent,
        IGameObjTouchEvent, 
        IGameObjMoveEvent,
        IGameObjScaleEvent,
        IGameObjRotateEvent,
        IGameObjCollisionEvent,
        ICameraMoveEvent,
        IFlingEvent,
        IScaleEvent,
        IScrollEvent,
        IKeyEvent,
        IWidgetTouchedBeginEvent,
        IWidgetTouchedEndEvent,
        IUserEvent {
    
    private IEventType mType;
    
    // generic data fields
    private int mIntA;
    private int mIntB;
    private int mIntC;
    private int mIntD;
    private float mFloatA;
    private float mFloatB;
    private float mFloatC;
    private float mFloatD;
    private boolean mBooleanA;
    
    private IGameState mGameState0;
    private IGameState mGameState1;
    private TouchAction mTouchAction;
    private GameObj mGameObj;
    private final float[] mVec = new float[4];
    private Object mObject0;
    
    
    public DefaultEvent(IGame game)
    {
        super(game);
        mType = IEvent.SystemEventType.INVALID;
    }
    
    @Override
    public void recycle() {
        super.recycle();  
        mType = IEvent.SystemEventType.INVALID;
        mGameState0 = null;
        mGameState1 = null;
        mGameObj = null;
        mObject0 = null;
    }

    // IEvent -----------------------------
    public IEventType getType()             {return mType;}
    public void setType(IEventType type) {mType = type;}
       
    // IStatsUpdateEvent -----------------------------
    public float getFramesPerSecond() {return mFloatA;}
    public float getPrevFramesPerSecond() {return mFloatB;}
    public float getMaxDelta() {return mFloatC;}
    public float getMinDelta() {return mFloatD;}
    public void setStatsData(float fps, float prev, float minDelta, 
            float maxDelta) {
        mFloatA = fps;
        mFloatB = prev;
        mFloatC = minDelta;
        mFloatD = maxDelta;
    }

    // IGameStateChangedEvent -------------
    public IGameState getNewState() {return mGameState0;}
    public IGameState getPrevState() {return mGameState1;}
    public void setGameStateData(IGameState newState, IGameState prevState) {
        mGameState0 = newState;
        mGameState1 = prevState;
    }
    
    // game object new and destroy event
    public GameObj getGameObj()             {return mGameObj;}
    public void setGameObj(GameObj obj) {
        mGameObj = obj;
    }
   
    // game object touch event ------------
    public float[] getTouchPoint() {return mVec;}
    public void setGameObjData(int objID, float[] point, TouchAction action, 
            int x, int y) {
        mIntC = objID;
        mTouchAction = action;
        mIntA = x;
        mIntB = y;
        mVec[0] = point[0];
        mVec[1] = point[1];
        mVec[2] = point[2];
        mVec[3] = 1;
    }
    
    public TouchAction getTouchAction()     {return mTouchAction;}
    public int getTouchX()                  {return mIntA;}
    public int getTouchY()                  {return mIntB;}
    public int getGameObjID()               {return mIntC;}
    
    // game object move event -------------
    public float[] getPosition()            {return mVec;}
    public void setGameObjData(int objID, float x, float y, float z) {
        mIntC = objID;
        mVec[0] = x;
        mVec[1] = y;
        mVec[2] = z;
        mVec[3] = 1;
    } 
    
    // game object scale event ------------
    public float[] getScale()               {return mVec;}
    
    // game object rotate event -----------
    public float[] getRotation()             {return mVec;}
    public void setGameObjData(int objID) {
        mIntC = objID;
    }
    
    // collision event  --------------------------------    
    public int getOtherGameObjID() {
        return mIntD;
    }
        
    public float[] getCollisionPoint() {
        return mVec;
    }

    public void setGameObjData(int objID, int otherObjID, float x, float y,
            float z) {
        mIntC = objID;
        mIntD = otherObjID;
        mVec[0] = x;
        mVec[1] = y;
        mVec[2] = z;
        mVec[3] = 1;
    }
    
    // ui touch event ---------------------
    public void setTouchData(TouchAction action, int x, int y) {
        mTouchAction = action;
        mIntA = x;
        mIntB = y;
    }

    // ui camera movement -----------------
    public float[] getCamPosition() {
        return mVec;
    }
    
    public void setCamPosition(float x, float y, float z) {
        mVec[0] = x;
        mVec[1] = y;
        mVec[2] = z;
        mVec[3] = 1;
    }

    // scroll gesture ------------------------------
    public float getDistanceX() {
        return mFloatA;
    }

    public float getDistanceY() {
        return mFloatB;
    }

    public void setScrollData(int x, int y, float distanceX, float distanceY) {
        mIntA = x;
        mIntB = y;
        mFloatA = distanceX;
        mFloatB = distanceY;
    }

    // scale gesture --------------------------------
    public float getScaleFactor() {
        return mFloatA;
    }
    
    public boolean isNewScaleGesture() {
        return mBooleanA;
    }

    public void setScaleData(float factor, boolean newGesture) {
        mFloatA = factor;
        mBooleanA = newGesture;
    }

    // fling gesture --------------------------------
    public float getVelocityX() {
        return mFloatA;
    }

    public float getVelocityY() {
        return mFloatB;
    }

    public void setFlingData(int x, int y, float velocityX, float velocityY) {
        mIntA = x;
        mIntB = y;
        mFloatA = velocityX;
        mFloatB = velocityY;
        
    }

    // key event -------------------------------------
    public int getAction() {return mIntA;}
    public int getKeyCode() {return mIntB;}

    public void setKeyData(int keyCode, int action) {
        mIntA = action;
        mIntB = keyCode;
    }
    
    // widget pressed event --------------------------
    public BaseTouchWidget getWidget() {
    	return (BaseTouchWidget) mObject0;
    }
    
	public void setWidgetData(BaseTouchWidget widget) {
		mObject0 = widget;
	}
    
    // user event ------------------------------------
    public Object getData() {
        return mObject0;
    }

    public void setUserData(Object data) {
        mObject0 = data;
    }
    
}
