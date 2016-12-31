package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.Game.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;

/**
 * Constant movement of a {@link GameObj}.  
 */
public class MotionModifier extends BaseProcess {
    
    private final IPositionable3D mObj;
    /* speed towards x in 1.0/ms */    
    private final float mXSpeed;
    /* speed towards y in 1.0/ms */
    private final float mYSpeed;
    /* speed towards z in 1.0/ms */
    private final float mZSpeed;
    private final ClockType mClock;
    
    /**
     * Constructs MotionModifier based on virtual clock.
     * @param obj Game object to move.
     * @param speed Moving speed towards direction (1.0/s)
     * @param direction Moving direction (normalized vector)
     */
    public MotionModifier(IPositionable3D obj, float speed, float[] direction) {
        this(obj, speed, direction, ClockType.VIRTUAL);
    }
    
    public MotionModifier(IPositionable3D obj, float speed, float[] direction, 
            ClockType clock) {
        mXSpeed = direction[0] * speed/1000;
        mYSpeed = direction[1] * speed/1000;
        mZSpeed = direction[2] * speed/1000;
        mObj = obj;
        mClock = clock;
    }

    @Override
    public void onInit() {}

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        final float[] pos = mObj.getPosition();
        final float delta = mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        final float x = pos[0] + mXSpeed * delta;
        final float y = pos[1] + mYSpeed * delta;
        final float z = pos[2] + mZSpeed * delta;
        mObj.setPosition(x, y, z);
       
        return true;
    }

    @Override
    protected void onKill() {
                
    }

}
