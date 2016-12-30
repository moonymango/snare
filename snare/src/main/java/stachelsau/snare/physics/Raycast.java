package stachelsau.snare.physics;

import java.util.ArrayList;

import stachelsau.snare.game.GameObj;
import stachelsau.snare.game.GameObj.GameObjLayer;
import stachelsau.snare.util.PoolItem;
import stachelsau.snare.util.VectorAF;

public class Raycast extends PoolItem {
            
    private final ArrayList<GameObj> mIntersections = new ArrayList<GameObj>();
    private final float[] mS = {0, 0, 0, 1};
    private final float[] mV = {1, 0, 0, 0};
    private boolean mIsInitialized;
    private int mNearestObjIdx;
    private float mNearestObjDist;
    private final float[] mNearestHitPoint = new float[4];
    private GameObjLayer mLayerMask;
    
    /**
     * Sets the ray's origin (S) and direction (V).
     * @param layer Layer of objects to test.
     * @param sx Ray origin x.
     * @param sy Ray origin y.
     * @param sz Ray origin z.
     * @param vx Ray direction x.
     * @param vy Ray direction y.
     * @param vz Ray direction z.
     * @param normalize True to normalize the direction vector, false otherwise.
     */
    public void init(GameObjLayer layerMask, float sx, float sy, float sz, 
            float vx, float vy, float vz, boolean normalize) {
        mLayerMask = layerMask;
        mS[0] = sx;
        mS[1] = sy;
        mS[2] = sz;
        mV[0] = vx;
        mV[1] = vy;
        mV[2] = vz;
        if (normalize) {
            VectorAF.normalize(mV);
        }
        mNearestObjDist = Float.MAX_VALUE;
        mIsInitialized = true;
    }
    
    protected GameObjLayer getLayerMask() {
        if (!mIsInitialized) {
            throw new IllegalStateException("Raycast not initialized.");
        }
        return mLayerMask;
    }
    
    /**
     * Returns ray's S component, i.e. the origin point.
     * @return
     */
    protected float[] getS() {
        if (!mIsInitialized) {
            throw new IllegalStateException("Raycast not initialized.");
        }
        return mS;
    }
    
    /**
     * Return's ray's V component, i.e. the direction.
     * @return
     */
    protected float[] getV() {
        if (!mIsInitialized) {
            throw new IllegalStateException("Raycast not initialized.");
        }
        return mV;
    }

    protected void addIntersection(float distance, GameObj obj) {
        if (distance < 0 || obj == null) {
            throw new IllegalArgumentException("Invalid distance or missing game object reference");
        }
        mIntersections.add(obj);
        if (distance < mNearestObjDist) {
            mNearestObjIdx = mIntersections.size() - 1;
            mNearestObjDist = distance;
            VectorAF.calcPointByRayDistance(mNearestHitPoint, mS, mV, distance);
        }
    }
        
    public GameObj getNearestHit() {
        if (!mIntersections.isEmpty()) {
            return mIntersections.get(mNearestObjIdx);
        }
        return null;
    }
    
    public float[] getNearestHitPoint() {
        if (!mIntersections.isEmpty()) {
            return mNearestHitPoint;
        }
        return null;
    }
    
    public boolean hasHits() {
        return !mIntersections.isEmpty();
    }
    
    @Override
    public void recycle() {
        mIntersections.clear();
        mIsInitialized = false;
        super.recycle();
    }
   
}
