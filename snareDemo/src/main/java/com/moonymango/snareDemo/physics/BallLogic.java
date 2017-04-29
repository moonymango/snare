package com.moonymango.snareDemo.physics;

import com.moonymango.snare.audio.AudioComponent;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.EventManager.IEventListener;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjCollisionEvent;
import com.moonymango.snare.events.IUserEvent;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.logic.BaseComponent;
import com.moonymango.snare.proc.DelayProc;
import com.moonymango.snare.util.VectorAF;
import com.moonymango.snareDemo.physics.GameState.PongObject;

class BallLogic extends BaseComponent implements IEventListener {

    private final float[] mDirection = new float[4];
    private float mSpeed = 0.01f;
    private AudioComponent mAudio;
    private final DelayProc mWait = new DelayProc(mGame, 2000);
    
    public BallLogic(IGame game) {
        super(game, ComponentType.LOGIC);
    }
    
    @Override
    public void onInit() {
        mGame.getEventManager().addListener(IGameObjCollisionEvent.EVENT_TYPE, this);
        mAudio = (AudioComponent) getGameObj().getComponent(ComponentType.AUDIO);
        mWait.run();
        setRandomDirection();
    }

    @Override
    public void onShutdown() {
        mGame.getEventManager().removeListener(IGameObjCollisionEvent.EVENT_TYPE, this);
    }

    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {
        final GameObj ball = getGameObj();
        final float[] pos = ball.getPosition();
        // calc new position, but do not move ball in case we wait for users attention
        float x = mWait.isRunning() ? pos[0] : pos[0] + virtualDelta * mSpeed * mDirection[0];
        float z = mWait.isRunning() ? pos[2] : pos[2] + virtualDelta * mSpeed * mDirection[2];
        
        // check if ball has left the grid
        PongObject missed = null;
        if (z > GameState.FRONT + 1) {
            missed = PongObject.PLAYER_PADDLE;
        }
        if (z < GameState.BACK - 1) {
            missed = PongObject.AI_PADDLE;
        }
        if (missed != null) {
            // place ball in center and wait some time before moving again
            x = 0;
            z = 0;
            setRandomDirection();
            mWait.run();
            
            // send event with paddle that missed the ball
            final EventManager em = mGame.getEventManager();
            final IUserEvent e = em.obtain(IUserEvent.EVENT_TYPE);
            e.setUserData(missed);
            em.queueEvent(e);
        }
        
        ball.setPosition(x, 0, z);
    }

    @Override
    public boolean handleEvent(IEvent event) {
        IGameObjCollisionEvent e = (IGameObjCollisionEvent) event;
        int id = e.getGameObjID();
        if (id == getGameObj().getID()) {
            id = e.getOtherGameObjID();
        }
        
        final GameObj go = mGame.getObjById(id);
        final PongObject po = PongObject.valueOf(go.getName());
        
        switch(po) {
        case LEFT_BORDER:
        case RIGHT_BORDER:
            mDirection[0] *= -1;
            break;
            
        case PLAYER_PADDLE:
        case AI_PADDLE:
            // TODO prioC: modify x component based on where the ball has hit the paddle
            //final float[] cp = e.getCollisionPoint();
            
            mDirection[2] *= -1;
            break;
            
        default:
            
        }  
        
        // play sound
        if (mAudio != null) {
            mAudio.play();
        }
        return false;
    }
    
    private void setRandomDirection() { 
        mDirection[0] = mGame.getRandomFloat(-0.5f, 0.5f);
        mDirection[1] = 0;
        mDirection[2] = mGame.getRandomFloat(-1, 1);
        mDirection[3] = 0;
        VectorAF.normalize(mDirection);
    }
    
}