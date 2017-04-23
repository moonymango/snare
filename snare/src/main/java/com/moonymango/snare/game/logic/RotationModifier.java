package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;


/**
 * Rotates a game object continously.
 */
public class RotationModifier extends BaseProcess {
    
    private final float[] mRotationVec = new float[3];
    private float mDegreesPerMilliSeconds;
    private IRotatable3D mObj;
    private IGame.ClockType mClock;
        
    /**
     * Constructs modifier based on virtual clock.
     * @param obj Obj to rotate.
     * @param x Rotation axis - X
     * @param y Rotation axis - Y
     * @param z Rotation axis - Z
     * @param degreesPerSecond Rotation speed
     */
    public RotationModifier(IGame game, IRotatable3D obj, float x, float y, float z, float degreesPerSecond)
    {
        super(game);
        configure(obj, x, y, z, degreesPerSecond, IGame.ClockType.VIRTUAL);
    }
            
    public RotationModifier(IGame game, IRotatable3D obj, float x, float y, float z, float degreesPerSecond,
                            IGame.ClockType clock)
    {
        super(game);
        configure(obj, x, y, z, degreesPerSecond, clock);
    }

    /**
     * Constructor for deferred configuration.
     * configure() must be called before running the process.
     */
    public RotationModifier(IGame game)
    {
        super(game);
    }

    /**
     * Configures modifier.
            * @param obj Obj to rotate.
            * @param x Rotation axis - X
     * @param y Rotation axis - Y
     * @param z Rotation axis - Z
     * @param degreesPerSecond Rotation speed
     */
    public RotationModifier configure(IRotatable3D obj, float x, float y, float z,
            float degreesPerSecond, IGame.ClockType clock) {

        if (obj == null) {
            throw new IllegalArgumentException("Missing game obj.");
        }

        mObj = obj;
        mRotationVec[0] = x;
        mRotationVec[1] = y;
        mRotationVec[2] = z;
        mDegreesPerMilliSeconds = degreesPerSecond / 1000;
        mClock = clock;

        return this;
    }


    @Override
    public void onInit() {
        if (mObj == null) {
            throw new IllegalStateException("configure() must be called before init.");
        }
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        final float delta = mClock == IGame.ClockType.REALTIME ? realDelta : virtualDelta;
        final float angle = (mDegreesPerMilliSeconds * delta) % 360;
        
        mObj.rotate(mRotationVec[0], mRotationVec[1], mRotationVec[2], angle);
        return true;
    }

    @Override
    protected void onKill() {
        
    }
  
}
