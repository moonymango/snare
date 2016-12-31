package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.ui.scene3D.BaseSceneDrawable;

/**
 * Process which simplifies handling of event based drawables,
 * e.g. explosions. The process adds the {@link GameObj} carrying
 * the {@link ISceneDrawable} to the game and removes it as soon
 * as the drawable is finished. The game object is then recycled
 * and the process can be started again.
 */
public class FireAndForgetEffectProc extends BaseProcess {

    private final GameObj mGameObj;
    private BaseSceneDrawable mDrawable;
    
    public FireAndForgetEffectProc(GameObj obj) {
        mGameObj = obj;
        mDrawable = (BaseSceneDrawable) obj.getComponent(ComponentType.RENDERING);
    }
    
    public GameObj getGameObj() {
        return mGameObj;
    }
    
    @Override
    protected void onInit() {
        Game.get().addGameObj(mGameObj);
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        return !mDrawable.isFinished();
    }

    @Override
    protected void onKill() {
        Game.get().removeGameObj(mGameObj);
        recycle();
    }

}
