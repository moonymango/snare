package com.moonymango.snareDemo.touch;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.logic.IPositionable3D;
import com.moonymango.snare.game.logic.MotionModifier;
import com.moonymango.snare.ui.scene3D.PerspectiveCamera;

public class CameraOperatorProc extends MotionModifier {

    private final PerspectiveCamera mCam;
    
    public CameraOperatorProc(IPositionable3D obj, float speed,
            float[] direction) {
        super(obj, speed, direction);
        mCam = (PerspectiveCamera) ((GameObj) obj).getComponent(ComponentType.CAMERA);
    }

    @Override
    public boolean onUpdate(long realTime, float realDelta, float virtualDelta) {
        boolean result = super.onUpdate(realTime, realDelta, virtualDelta); 
        
        // adjust look-at position
        final float[] pos = mCam.getPosition();
        mCam.lookAt(0, 0, pos[2]-15, 0, 1, 0);
        return result;
    }

}
