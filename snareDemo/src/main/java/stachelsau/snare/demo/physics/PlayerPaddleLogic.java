package stachelsau.snare.demo.physics;

import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.logic.ScrollLogicComponent.IScrollHandler;

class PlayerPaddleLogic implements IScrollHandler {

    @Override
    public void handleScroll(GameObj obj, int x, int y, float distanceX,
            float distanceY) {
        final float[] pos = obj.getPosition();
        float new_x = pos[0] - distanceX/100;
        
        new_x = new_x < GameState.MIN_PADDLE_X ? GameState.MIN_PADDLE_X : new_x;
        new_x = new_x > GameState.MAX_PADDLE_X ? GameState.MAX_PADDLE_X : new_x; 
        
        obj.setPosition(new_x, pos[1], pos[2]);
    }
}