package com.moonymango.snare.physics;

import android.util.SparseArray;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.IEvent;
import com.moonymango.snare.events.IGameObjCollisionEvent;
import com.moonymango.snare.events.IGameObjDestroyEvent;
import com.moonymango.snare.events.IGameObjNewEvent;
import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.GameObj.GameObjLayer;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.physics.BaseBoundingVolume.IntersectionDistance;
import com.moonymango.snare.physics.BaseBoundingVolume.VolumeType;
import com.moonymango.snare.util.Pool;

import java.util.ArrayList;

/**
 * Provides raycasting and collision detection.
 * Implementation is probably not very efficient, so it is intended for cases 
 * when only a small number of objects have to be tested for collisions.
 * No motion, no forces or anything like that.
 */
public class SimplePhysics extends BaseSnareClass implements IPhysics {
    
    private RaycastPool mRaycastPool;
    private PairPool mPairPool;
    /** List of all bounding volumes. */
    private ArrayList<BaseSimpleBoundingVolume> mBoundingVolumes = new ArrayList<BaseSimpleBoundingVolume>();
    /** List of bounding volumes that want to be checked for collisions with others */
    private ArrayList<BaseSimpleBoundingVolume> mCollisionCheckers = new ArrayList<BaseSimpleBoundingVolume>();
    
    // two list alternately used for current and previous collisions
    private final SparseArray<SimpleCollisionPair> mMapA = new SparseArray<SimpleCollisionPair>();
    private final SparseArray<SimpleCollisionPair> mMapB = new SparseArray<SimpleCollisionPair>();
    private SparseArray<SimpleCollisionPair> mCurrentCollisions = mMapA;
    private SparseArray<SimpleCollisionPair> mPreviousCollisions = mMapB;
    
    private final ArrayList<SimpleCollisionPair> mListA = new ArrayList<SimpleCollisionPair>();
    private final ArrayList<SimpleCollisionPair> mListB = new ArrayList<SimpleCollisionPair>();
    private ArrayList<SimpleCollisionPair> mCurrentIterator = mListA;
    private ArrayList<SimpleCollisionPair> mPreviousIterator = mListB;
    
    private boolean mCollisionChecking;
    
    public SimplePhysics(IGame game)
    {
        super(game);
        mRaycastPool = new RaycastPool(game);
        mPairPool = new PairPool(game);
    }
    
    public void enableCollisionChecking(boolean enable) {
        mCollisionChecking = enable;
    }

    public void onInit() {
        SnareGame.get().getEventManager().addListener(IGameObjNewEvent.EVENT_TYPE, this);
        SnareGame.get().getEventManager().addListener(IGameObjDestroyEvent.EVENT_TYPE, this);
    }

    public void tick(long realTime, float realDelta, float virtualDelta) {
        if (!mCollisionChecking) {
            return;
        }
        // collision test:
        // 1. collect pairs to test in list mCurrentCollisions
        for (int i = mCollisionCheckers.size() - 1; i >= 0; i--) {
            final BaseSimpleBoundingVolume bvA = mCollisionCheckers.get(i);
            final GameObjLayer layerA = bvA.getGameObj().getLayer();
            for (int j = mBoundingVolumes.size() - 1; j >= 0; j--) {
                final BaseSimpleBoundingVolume bvB = mBoundingVolumes.get(j);
                final GameObjLayer layerB = bvB.getGameObj().getLayer();
                // do not test a volume against itself and only test if
                // there is an intersection in layer masks
                if (bvA == bvB || !layerB.covers(layerA)) {    
                    continue;
                }
                
                // check if both bounding volumes are already scheduled
                // for testing
                final int hash = SimpleCollisionPair.calcHash(bvA, bvB);
                if (mCurrentCollisions.get(hash) == null) {
                    final SimpleCollisionPair p = mPairPool.obtain();
                    p.init(bvA, bvB);
                    mCurrentCollisions.put(hash, p);
                    mCurrentIterator.add(p);
                } 
            }
        }
        
        // 2. remove all pairs from mCurrentCollisions that are actually not
        //    collisions
        for (int i = mCurrentIterator.size() - 1; i >= 0; i--) {
            final SimpleCollisionPair p = mCurrentIterator.get(i);
            if (!p.isCollision()) {
                mCurrentIterator.remove(i);
                mCurrentCollisions.remove(p.hashCode());
                p.recycle();
            }
        }
        
        // 3. compare to previous collisions
        for (int i = mCurrentIterator.size() - 1; i >= 0; i--) {
            final ICollisionPair p = mCurrentIterator.get(i);
            if (mPreviousCollisions.get(p.hashCode()) == null) {
                // new collision, send collision event
                final EventManager em = SnareGame.get().getEventManager();
                IGameObjCollisionEvent e = em.obtain(IGameObjCollisionEvent.EVENT_TYPE);
                final float[] cp = p.getCollisionPoint();
                e.setGameObjData(p.getObjIdA(), p.getObjIdB(), cp[0], cp[1], cp[2]);
                em.queueEvent(e);
            }
        }
        
        // 4. swap collision lists
        for (int i = mPreviousIterator.size() - 1; i >= 0; i--) {
            mPreviousIterator.get(i).recycle();
        }
        mPreviousIterator.clear();
        mPreviousCollisions.clear();
        
        mCurrentCollisions = mCurrentCollisions == mMapA ? mMapB : mMapA;
        mPreviousCollisions = mPreviousCollisions == mMapA ? mMapB : mMapA;
        mCurrentIterator = mCurrentIterator == mListA ? mListB : mListA;
        mPreviousIterator = mPreviousIterator == mListA ? mListB : mListA;
        
    }

    public Raycast doRaycast(float[] s, float[] v, GameObjLayer layerMask) {
        Raycast r = mRaycastPool.obtain();
        r.init(layerMask, s[0], s[1], s[2], v[0], v[1], v[2], true);
        
        for (int i = mBoundingVolumes.size() - 1; i >= 0; i--) {
            final BaseSimpleBoundingVolume bv = mBoundingVolumes.get(i);
            final boolean covered = layerMask.covers(bv.getGameObj().getLayer());
            if (!covered || !bv.isRaycastEnabled()) {
                // object is not in a layer of interest
                continue;
            }
            
            final IntersectionDistance d = bv.getRayDistance(s, v);
            // only consider object that are completely in front of the ray origin, i.e.
            // ignore object, if the ray origin is within the bounding volume and also ignore
            // if object is behind ray origin
            if (d != null && d.MIN > 0) {
                r.addIntersection(d.MIN, bv.getGameObj());
            }
        }
        
        return r;
    }
    
    public BaseBoundingVolume createBoundingVolume(VolumeType type) {
        // enable collision checking by default
        switch(type) {
        case BOX:
            return new SimpleAABB(true);
        case SPHERE:
            return new SimpleSphereBoundingVolume(true);
        case CYLINDER:
            return new SimpleCylinderBoundingVolume(true);
        case SQUARE:
            return new SimpleSquareBoundingVolume();
        default:
            throw new IllegalArgumentException("Unsupported bounding volume type.");
        }
    }

    public boolean handleEvent(IEvent event) {
        if (event.getType().equals(IGameObjNewEvent.EVENT_TYPE)) {
            final GameObj obj = ((IGameObjNewEvent) event).getGameObj();
            final BaseSimpleBoundingVolume bv = (BaseSimpleBoundingVolume) obj.getComponent(ComponentType.BOUNDING_VOLUME);
            if (bv != null) {
                mBoundingVolumes.add(bv);
                if (bv.isCollisionChecker()) {
                    mCollisionCheckers.add(bv);
                }
            }
        }
        
        if (event.getType().equals(IGameObjDestroyEvent.EVENT_TYPE)) { 
            final GameObj obj = ((IGameObjDestroyEvent) event).getGameObj();
            if (obj == null) {
                return false;
            }
            final BaseBoundingVolume bv= (BaseBoundingVolume) obj.getComponent(ComponentType.BOUNDING_VOLUME);
            if (bv != null) {
                mBoundingVolumes.remove(bv);
                if (bv.isCollisionChecker()) {
                    mCollisionCheckers.remove(bv);
                }
            }
        }
        return false;
    }


    private static class RaycastPool extends Pool<Raycast>
    {
        public RaycastPool(IGame game)
        {
            super(game);
        }

        @Override
        protected Raycast allocatePoolItem() {
            return new Raycast(mGame);
        }

    }
    
    private static class PairPool extends Pool<SimpleCollisionPair>
    {
        public PairPool(IGame game)
        {
            super(game);
        }

        @Override
        protected SimpleCollisionPair allocatePoolItem() {
            return new SimpleCollisionPair(mGame);
        }
        
    }
}
