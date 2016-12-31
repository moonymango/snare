package com.moonymango.snare.game.logic;

import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjMoveEvent;
import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

/**
 * Let a {@link GameObj} follow another object.
 */
public class FollowMotionModifier extends BaseProcess implements IEventListener {

    private final GameObj mToFollowObj;
    private final IPositionable3D mFollower;
    private final float mOffsX;
    private final float mOffsY;
    private final float mOffsZ;
    private final float[] mPos = new float[4];
    private boolean mHasChanged;
    
    /**
     * 
     * @param toFollow Object to be followed.
     * @param follower Follower.
     * @param offsX x offset.
     * @param offsY y offset.
     * @param offsZ z offset.
     */
    public FollowMotionModifier(GameObj toFollow, IPositionable3D follower, 
            float offsX, float offsY, float offsZ) {
        mToFollowObj = toFollow;
        mFollower = follower;
        mOffsX = offsX;
        mOffsY = offsY;
        mOffsZ = offsZ;
    }
    
    @Override
    public boolean handleEvent(IEvent event) {
        final IGameObjMoveEvent e = (IGameObjMoveEvent) event;
        if (e.getGameObjID() != mToFollowObj.getID()) {
            return false;
        }
        final float[] pos = mToFollowObj.getPosition();
        mPos[0] = pos[0];
        mPos[1] = pos[1];
        mPos[2] = pos[2];
        mPos[3] = pos[3];
        mHasChanged = true;
        return false;
    }

    @Override
    protected void onInit() {
        Game.get().getEventManager().addListener(IGameObjMoveEvent.EVENT_TYPE,
                this);
        final float[] pos = mToFollowObj.getPosition();
        mPos[0] = pos[0];
        mPos[1] = pos[1];
        mPos[2] = pos[2];
        mPos[3] = pos[3];
        mHasChanged = true;
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        if (mHasChanged) {
            mFollower.setPosition(mPos[0] + mOffsX, mPos[1] + mOffsY, mPos[2] + mOffsZ);
            mHasChanged = false;
        }
        return true;
    }

    @Override
    protected void onKill() {
        Game.get().getEventManager().removeListener(IGameObjMoveEvent.EVENT_TYPE,
                this);
    }

}
