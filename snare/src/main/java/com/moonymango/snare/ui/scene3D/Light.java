package com.moonymango.snare.ui.scene3D;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.IComponent;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.ColorWrapper.IColorSeqListener;

//TODO prioC: add parameters to Light for SPOT
/**
 * Light component for game objects. Shares the position of the game object. 
 * When used as directional light, the direction is the objects forward
 * vector.
 */
public class Light implements IComponent, IColorSeqListener {
    
    private static final float[] DEFAULT_POSITION = {0, 0, 0, 1};
    
    private final LightType mType;
    private final float[] mColor = {0, 0, 0, 0};
    
    private GameObj mGameObj;
        
    public Light(LightType type) {
        mType = type;
    }
    
    public LightType getType() {
        return mType;
    }
       
    /**
     * Returns the light's color and attenuation in a vector as follows <br/>
     * Indices 0..2 = r,g,b <br/>
     * Index 3 = attenuation <br/>
     * Note: for light types without position attenuation will always be 0.
     * @return
     */
    public float[] getColor() {
        return mColor;
    }
        
    /**
     * Sets color and attenuation. Note: for light types that do not have a position
     * attenuation is set to 0 regardless of value given here.
     * @param r
     * @param g
     * @param b
     * @param att
     */
    public void setColor(float r, float g, float b, float att) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        switch(mType) {
        case POINT:
        case SPOT:
            mColor[3] = att;
            break;
        default: 
            mColor[3] = 0; // no attenuation for ambient light
        }
    }
    
    public float[] getDirection() {
        switch(mType) {
        case DIRECTIONAL:
        case SPOT:    
            return mGameObj.getForwardVector();
            
        default:
            throw new UnsupportedOperationException("Light type doesn't support directions.");
        }
    }
    
    public float[] getPosition() {
        return mGameObj == null ? DEFAULT_POSITION : mGameObj.getPosition();
    }

    public void onAttachToScene() {}

    public void onDetachFromScene() {}
    
    @Override
    public void onColorChange(ColorWrapper cp) {
        final float[] c = cp.getActualColor();
        setColor(c[0], c[1], c[2], c[3]);
    }

    @Override
    public ComponentType getComponentType() {return ComponentType.LIGHT;}

    @Override
    public void onUpdate(long realTime, float realDelta, float virtualDelta) {}

    @Override
    public void onTraverse(Object userData) {}

    @Override
    public void onInit() {}

    @Override
    public void onShutdown() {}

    @Override
    public void reset() {}

    @Override
    public GameObj getGameObj() {return mGameObj;}

    @Override
    public void setGameObj(GameObj obj) {mGameObj = obj;}



    public enum LightType {
        /** Light w/o position or direction. */
        AMBIENT,
        /** Light with direction only, i.e. light from an infinitely distant source */
        DIRECTIONAL,
        /** Light with position only. */
        POINT,
        /** Light with position and direction. */
        SPOT
    }
     
}
