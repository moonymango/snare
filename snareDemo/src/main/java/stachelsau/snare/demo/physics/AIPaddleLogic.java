package stachelsau.snare.demo.physics;

import stachelsau.snare.events.EventManager.IEventListener;
import stachelsau.snare.events.IEvent;
import stachelsau.snare.events.IGameObjMoveEvent;
import stachelsau.snare.game.Game;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.ComponentType;
import stachelsau.snare.game.logic.BaseComponent;

class AIPaddleLogic extends BaseComponent implements IEventListener {
    
    private int mDirection;
    private float mSpeed = 0.004f;
    private int mBallID;
    
    public AIPaddleLogic() {
        super(ComponentType.LOGIC);
    }

    @Override
    public void onInit() {
        Game.get().getEventManager().addListener(IGameObjMoveEvent.EVENT_TYPE, this);
        // hack to retrieve id of the ball without listening to events 
        mBallID = ((GameState) Game.get().getGameState()).mBall.getID();
    }

    @Override
    public void onShutdown() {
        Game.get().getEventManager().removeListener(IGameObjMoveEvent.EVENT_TYPE, this);
        
    }

    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {
        final GameObj paddle = getGameObj();
        final float[] pos = paddle.getPosition();
        float x = pos[0] + virtualDelta * mSpeed * mDirection;
        
        x = x < GameState.MIN_PADDLE_X ? GameState.MIN_PADDLE_X : x;
        x = x > GameState.MAX_PADDLE_X ? GameState.MAX_PADDLE_X : x; 
        
        paddle.setPosition(x, pos[1], pos[2]);
    }

    @Override
    public boolean handleEvent(IEvent event) {
        IGameObjMoveEvent e = (IGameObjMoveEvent) event;
        final int id = e.getGameObjID();
        if (id != mBallID) {
            return false;
        }
        final float[] ballPos = e.getPosition();
        final float[] aiPaddlePos = getGameObj().getPosition();
        
        // move paddle towards the ball
        if (ballPos[0] > aiPaddlePos[0]) {
            mDirection = 1;
        } else {
            mDirection = -1;
        }
        
        return false;
    }
    
    
    
}