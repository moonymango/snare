package com.moonymango.snare.game;


public abstract class BaseGameView extends BaseSnareClass {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    private static int sInstanceCnt = 0;
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    public final int mViewID;
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public BaseGameView(IGame game)
    {
        super(game);
        mViewID = ++sInstanceCnt;
    }
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public abstract void onInit();
    public abstract void onShutdown();
    public abstract void onUpdate(long realTime, float realDelta, float virtualDelta);
    
    public int getID() {
        return mViewID;
    }

      
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseGameView)) {
            return false;
        }
        final BaseGameView view = (BaseGameView) o;
        return view.mViewID == this.mViewID;
    }

    @Override
    public int hashCode() {
        return mViewID;
    }
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
   
}
