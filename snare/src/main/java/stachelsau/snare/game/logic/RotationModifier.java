package stachelsau.snare.game.logic;

import stachelsau.snare.game.Game.ClockType;
import stachelsau.snare.proc.ProcessManager.BaseProcess;


/**
 * Rotates a game object continously.
 */
public class RotationModifier extends BaseProcess {
    
    private final float[] mRotationVec;
    private final float mDegreesPerMilliSeconds;
    private final IRotatable3D mObj;
    private final ClockType mClock;
        
    /**
     * Constructs modifier based on virtual clock.
     * @param obj Obj to rotate.
     * @param x Rotation axis - X
     * @param y Rotation axis - Y
     * @param z Rotation axis - Z
     * @param degreesPerSecond Rotation speed
     */
    public RotationModifier(IRotatable3D obj, float x, float y, float z, 
            float degreesPerSecond) {
        this(obj, x, y, z, degreesPerSecond, ClockType.VIRTUAL);
    }
            
    public RotationModifier(IRotatable3D obj, float x, float y, float z, 
            float degreesPerSecond, ClockType clock) {
        if (obj == null) {
            throw new IllegalArgumentException("Missing game obj.");
        }
        mObj = obj;
        mRotationVec = new float[3];
        mRotationVec[0] = x;
        mRotationVec[1] = y;
        mRotationVec[2] = z;
        mDegreesPerMilliSeconds = degreesPerSecond / 1000;
        mClock = clock;
    }
    
    @Override
    public void onInit() {}

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        final float delta = mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        final float angle = (mDegreesPerMilliSeconds * delta) % 360;
        
        mObj.rotate(mRotationVec[0], mRotationVec[1], mRotationVec[2], angle);
        return true;
    }

    @Override
    protected void onKill() {
        
    }
  
}
