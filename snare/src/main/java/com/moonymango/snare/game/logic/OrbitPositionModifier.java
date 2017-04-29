package com.moonymango.snare.game.logic;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGame.ClockType;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.util.MatrixAF;

/**
 * Orbits a game object around an arbitrary axis defined by a 
 * point and a vector. 
 */
public class OrbitPositionModifier extends BaseProcess {

    private final float[] mPoint = {0, 0, 0, 1};
    private final float[] mVec = {1, 0, 0, 0};
    private final float[] mTransform = new float[16];
    private final float[] mObjPosition = new float[4];
    
    private final IPositionable3D mObj;
    private final float mDegreesPerMillisecond;
    private float mAngle;
    private final IGame.ClockType mClock;
    
    /**
     * Constructs modifier based on virtual clock.
     * @param obj Object to orbit
     * @param axisPoint Point on the rotation axis
     * @param axisVector Vector of the rotation axis
     * @param degreesPerSecond Orbit speed
     */
    public OrbitPositionModifier(IGame game,IPositionable3D obj, float[] axisPoint, float[] axisVector,
                                 float degreesPerSecond)
    {
        this(game, obj, axisPoint, axisVector, degreesPerSecond, ClockType.VIRTUAL);
    }
    
    /**
     * Constructs modifier with axis point set to origin, i.e. the rotation
     * axis goes through origin. Uses virtual clock.
     * @param obj
     * @param axisVector
     * @param degreesPerSecond
     */
    public OrbitPositionModifier(IGame game, IPositionable3D obj, float[] axisVector, float degreesPerSecond)
    {
        this(game, obj, null, axisVector, degreesPerSecond, ClockType.VIRTUAL);
    }
    
    public OrbitPositionModifier(IGame game, IPositionable3D obj, float[] axisPoint, float[] axisVector,
                                 float degreesPerSecond, ClockType clock)
    {
        super(game);
        if (obj == null) {
            throw new IllegalArgumentException("Missing game object.");
        }
        
        mObj = obj;
        mDegreesPerMillisecond = degreesPerSecond / 1000;
        if (axisPoint != null) {
            mPoint[0] = axisPoint[0];
            mPoint[1] = axisPoint[1];
            mPoint[2] = axisPoint[2];
            mPoint[3] = axisPoint[3];    
        }
        if (axisVector != null) {
            mVec[0] = axisVector[0];
            mVec[1] = axisVector[1];
            mVec[2] = axisVector[2];
            mVec[3] = axisVector[3];
        }
        mClock = clock != null ? clock : ClockType.REALTIME;
    }
    
    @Override
    public void onInit() {}

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        final float delta = mClock == ClockType.REALTIME ? realDelta : virtualDelta;
        mAngle = mDegreesPerMillisecond * delta;
        
        // transform: translate to origin, rotate and translate back
        MatrixAF.setIdentityM(mTransform, 0);
        MatrixAF.translateM(mTransform, 0, mPoint[0], mPoint[1], mPoint[2]);
        MatrixAF.rotateM(mTransform, 0, mAngle, mVec[0], mVec[1], mVec[2]);
        MatrixAF.translateM(mTransform, 0, -mPoint[0], -mPoint[1], -mPoint[2]);
        
        // get the new position
        MatrixAF.multiplyMV(mObjPosition, 0, mTransform, 0, mObj.getPosition(), 0);
        
        mObj.setPosition(mObjPosition[0], mObjPosition[1], mObjPosition[2]);
        
        return true;
    }

    @Override
    protected void onKill() {
        
    }

}
