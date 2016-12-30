package stachelsau.snare.game.logic;

import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.ComponentType;

/**
 * Component that allows decoration of {@link GameObj} with arbitrary data.
 */
public class ScratchPadComponent extends BaseComponent {

    private final Object[] mObjs;
    
    public ScratchPadComponent(int numObjs) {
        super(ComponentType.SCRATCH_PAD);
        mObjs = new Object[numObjs];
    }

    public Object getObject(int index) {
        return mObjs[index];
    }
    
    public void setData(int index, Object obj) {
        mObjs[index] = obj;
    }

    @Override
    public void reset() {
        // clear all references
        for (int i = mObjs.length-1; i >= 0; i--) {
            mObjs[i] = null;
        }
    }
    
    
}
