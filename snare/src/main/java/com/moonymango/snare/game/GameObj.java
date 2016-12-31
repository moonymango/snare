/**
 * 
 */
package com.moonymango.snare.game;

import java.util.ArrayList;

import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.events.IGameObjMoveEvent;
import com.moonymango.snare.events.IGameObjRotateEvent;
import com.moonymango.snare.events.IGameObjScaleEvent;
import com.moonymango.snare.game.logic.IPositionable3D;
import com.moonymango.snare.game.logic.IRotatable3D;
import com.moonymango.snare.game.logic.IScalable3D;
import com.moonymango.snare.util.IMimic;
import com.moonymango.snare.util.MatrixAF;
import com.moonymango.snare.util.PoolItem;
import com.moonymango.snare.util.QuaternionAF;

/**
 *  Represents a game object with position, rotation and scale. Also
 *  holds an array of components which add more properties to an object.
 *  Note: {@link GameObj} is updated in two stages by the game loop:
 *      - first all components of are updated (and components of other game
 *        objects) 
 *      - after that the transformation are updated, i.e. the current settings
 *        of position, rotation and scale are used to produce the "toWorld"
 *        matrix. 
 */
public class GameObj extends PoolItem implements IPositionable3D,
        IRotatable3D,
        IScalable3D,
        IMimic<GameObj> {
    
    public static final int INVALID_GAME_OBJ_ID = -1;
    public static final GameObjLayer DEFAULT_OBJ_LAYER = new GameObjLayer("default", 0xffffffff);
    private static int sInstanceCnt;
               
    private final int mID;
    private final GameObjLayer mLayer;
    private boolean mIsInitialized;
    private final String mName;    
    // array of lists to store components
    // (use array to speed up component access and because number of component 
    //  types is constant) 
    @SuppressWarnings("unchecked")
    private final ArrayList<IComponent>[] mComponents = new ArrayList[ComponentType.getCount()];
    
    private final float[] mPosition     = {0, 0, 0, 1};  // xyz
    // switched to matrix representation for rotation,
    // but quaternion representation remains as comments in code
    //private final float[] mRotationQ    = new float[4];  // quaternion wxyz
    private final float[] mRotation     = new float[16];
    private final float[] mVec          = new float[4];
    private final float[] mScale        = {1, 1, 1, 0};  // scale xyz
    
    private float[] mToWorld = new float[16];
    private float[] mFromWorld = new float[16];
    private boolean mFromWorldValid = true;
    
    private boolean mMoved;
    private boolean mScaled;
    private boolean mRotated;
    private boolean mSendMoveEvent;
    private boolean mSendScaleEvent;
    private boolean mSendRotateEvent;
    private float mLastModTime;
    
    /**
     * Constructs game object with default layer.
     * Default layer enables the obj for all collision checking and
     * raycasting.
     * @param name
     */
    public GameObj(String name) {
        this(name, sInstanceCnt++, DEFAULT_OBJ_LAYER);
    }
    
    /**
     * Constructs game object with custom layer.
     * @param name
     * @param layer
     */
    public GameObj(String name, GameObjLayer layer) {
        this(name, sInstanceCnt++, layer);
    }
    
    protected GameObj(String name, int id, GameObjLayer layer) {
        if (name == null) {
            throw new IllegalArgumentException("Missing object name.");
        }
        mName = name;
        MatrixAF.setIdentityM(mToWorld, 0);
        MatrixAF.setIdentityM(mFromWorld, 0);
        //QuaternionAF.setIdentity(mRotationQ);
        MatrixAF.setIdentityM(mRotation, 0);
        mID = id;
        mLayer = layer;
        
        // set up components array
        for (int i = 0; i < mComponents.length; i++) {
            mComponents[i] = new ArrayList<IComponent>();
        }
    }
    
    public String getName()         {return mName;}
    public int getID()              {return mID;}
    public GameObjLayer getLayer()  {return mLayer;}
    
    public GameObj enableEvents(boolean sendScaleEvent,
            boolean sendRotateEvent, boolean sendMoveEvent) {
        mSendMoveEvent = sendMoveEvent;
        mSendRotateEvent = sendRotateEvent;
        mSendScaleEvent = sendScaleEvent;
        return this;
    }
    
    public GameObj addComponent(IComponent cpt) {
        if (cpt == null) {
            throw new IllegalArgumentException("Missing component.");
        }
        if (mIsInitialized) {
            throw new IllegalStateException("Cannot add components to " +
            		"initialized game object.");
        }
        if (cpt.getGameObj() != null) {
            throw new IllegalArgumentException("Component already registered " +
            		"to another game object.");
        }
        
        final ComponentType t = cpt.getComponentType();
        final int idx = t.index();
        final ArrayList<IComponent> lst = mComponents[idx];
        if (t.isUnique() && lst.size() != 0) {
            throw new IllegalStateException("Cannot add another component of " +
            		"unique type " + t.name() + " to object " + mName);
        }
        lst.add(cpt);
        cpt.setGameObj(this);
        return this;
    }
    
    /**
     * Returns first component of specified type.
     * @param type
     * @return {@link IComponent} or null in case no such component available.
     */
    public IComponent getComponent(ComponentType type) {
        final ArrayList<IComponent> lst = mComponents[type.index()];
        return lst.size() > 0 ? lst.get(0) : null; 
    }
    
    /**
     * Returns number of components of specified type.
     * @param type
     * @return
     */
    public int getComponentCnt(ComponentType type) {
        final ArrayList<IComponent> lst = mComponents[type.index()];
        return lst.size();
    }
    
    /**
     * Returns component of specified type at specified index. 
     * For non-unique components only.
     * @param type
     * @param idx 
     * @return
     */
    public IComponent getComponent(ComponentType type, int idx) {
        final ArrayList<IComponent> lst = mComponents[type.index()];
        return lst.get(idx);
    }
    
    public void onUpdateComponents(long realTime, float realDelta, 
            float virtualDelta) {
        int len = mComponents.length;
        for (int i = 0; i < len; i++) {
            final ArrayList<IComponent> lst = mComponents[i];
            for (int c = 0; c < lst.size(); c ++) {
                lst.get(c).onUpdate(realTime, realDelta, virtualDelta);
            }
        }
    }
    
    public void onUpdateTransform(long realTime, float realDelta, 
            float virtualDelta) { 
        if (mMoved || mScaled || mRotated) {
            mLastModTime = realTime;
            updateTransform();
        }
    }
    
    public void onInit() {
        if (mIsInitialized) {
            throw new IllegalStateException("Tried to initialize already " +
            		"initialized GameObj " + mName);
        }
        final int len = mComponents.length;
        for (int i = 0; i < len; i++) {
            final ArrayList<IComponent> lst = mComponents[i];
            for (int c = 0; c < lst.size(); c++) {
                lst.get(c).onInit();
            }
        }
        mIsInitialized = true;
    }
    
    public void onShutdown() {
        if (!mIsInitialized) {
            throw new IllegalStateException("Tried to shutdown " +
            		"non-initialized GameObj " + mName);
        }
        final int len = mComponents.length;
        for (int i = 0; i < len; i++) {
            final ArrayList<IComponent> lst = mComponents[i];
            for (int c = 0; c < lst.size(); c++) {
                lst.get(c).onShutdown();
            }
        }
        mIsInitialized = false;
    }
    
    /** 
     * Reset all components. Intended to achieve a default state when 
     * game obj is returned to a pool for reuse. 
     */
    public void reset() {
        final int len = mComponents.length;
        for (int i = 0; i < len; i++) {
            final ArrayList<IComponent> lst = mComponents[i];
            for (int c = 0; c < lst.size(); c++) {
                lst.get(c).reset();
            }
        }
    }
    
    @Override
    public void recycle() {
        reset();
        super.recycle();
    }

    public float[] getPosition() {
        return mPosition;
    }
    
    public GameObj setPosition(float x, float y, float z) {
        mPosition[0] = x;
        mPosition[1] = y;
        mPosition[2] = z;
        mMoved = true;
        return this;
    }
    
    public GameObj setPostion(float[] pos) {
        mPosition[0] = pos[0];
        mPosition[1] = pos[1];
        mPosition[2] = pos[2];
        mMoved = true;
        return this;
    }
    
    /** Uniform scale */
    public GameObj setScale(float scale) {
        // setting to 0 would result in non-invertible to-world matrix
        final float s = scale == 0 ? 0.0001f : scale;
        mScale[0] = s;
        mScale[1] = s;
        mScale[2] = s;
        mScaled = true;
        return this;
    }
    
    public GameObj setScale(float[] scale) {
        for (int i = 0; i < 3; i++) {
            mScale[i] = scale[i] == 0 ? 0.0001f : scale[i];
        }
        mScaled = true;
        return this;
    }
    
    public GameObj setScale(float x, float y, float z) {
        mScale[0] = x == 0 ? 0.0001f : x;
        mScale[1] = y == 0 ? 0.0001f : y;
        mScale[2] = z == 0 ? 0.0001f : z;
        mScaled = true;
        return this;
    }
    
    public float[] getScale() {
        return mScale;
    }
    
    /**
     * Rotates the object. All rotations are accumulated.
     */
    public GameObj rotate(float x, float y, float z, float angle) {
        //QuaternionAF.lhsRotateQ(mRotationQ, x, y, z, angle);
        MatrixAF.lhsRotateM(mRotation, 0, angle, x, y, z);
        mRotated = true;
        return this;
    }
    
    /**
     * Sets the rotation based on the objects local left, up and forward 
     * vectors. Axes must be orthogonal and of unit length, so be 
     * sure you know what you are doing!
     * @param left
     * @param up
     * @param forward
     */
    public GameObj setRotation(float[] left, float[] up, float[] forward) {
        mRotation[0] = left[0];
        mRotation[1] = left[1];
        mRotation[2] = left[2];
        mRotation[3] = 0;
        mRotation[4] = up[0];
        mRotation[5] = up[1];
        mRotation[6] = up[2];
        mRotation[7] = 0;
        mRotation[8] = forward[0];
        mRotation[9] = forward[1];
        mRotation[10] = forward[2];
        mRotation[11] = 0;
        mRotation[12] = 0;
        mRotation[13] = 0;
        mRotation[14] = 0;
        mRotation[15] = 1;
        mRotated = true;
        return this;
    }
    
    /** 
     * Sets rotation matrix directly. Be sure you know what you're doing!
     * @param mat
     * @return
     */
    public GameObj setRotation(float[] mat) {
        System.arraycopy(mat, 0, mRotation, 0, 16);
        mRotated = true;
        return this;
    }
    
    /**
     * Set rotation using quaternions. Quaternion must be of unit length. 
     * @param q 
     */
    public GameObj setRotationQ(float[] q) {
        final float[] mat = QuaternionAF.toMatrix(q);
        System.arraycopy(mat, 0, mRotation, 0, 16);
        mRotated = true;
        return this;
    }
    
    /**
     * Reset rotation to the local orientation.
     */
    public GameObj resetRotation() {
        //QuaternionAF.setIdentity(mRotationQ);
        MatrixAF.setIdentityM(mRotation, 0);
        mRotated = true;
        return this;
    }
    
    /**
     * Rotation in matrix representation.
     * @return
     */
    public float[] getRotation() {
        //return QuaternionAF.toAxisAngle(mRotationQ);
        return mRotation;
    }
    
    /**
     * Rotation in quaternion representation.
     * @return
     */
    public float[] getRotationQ() {
        final float[] q = QuaternionAF.fromMatrix(mRotation);
        for (int i = 0; i < 4; i++) {
            mVec[i] = q[i];
        }
        return mVec;
    }
    
    /**
     * Places the object at same coordinates with same rotation and scale
     * as the specified obj. 
     * @param obj
     * @return
     */
    public GameObj placeAt(GameObj obj) {
        final float[] pos = obj.getPosition();
        setPosition(pos[0], pos[1], pos[2]);
        final float[] rot = obj.getRotation();
        setRotation(rot);
        final float[] scale = obj.getScale();
        setScale(scale[0], scale[1], scale[2]);
        return this;
    }
    
    /**
     * Gets orientation up vector. Points to temporary storage and will
     * not be valid after call to another getter of {@link GameObj}.  
     * @return
     */
    public float[] getUpVector() {
        mVec[0] = mRotation[4];
        mVec[1] = mRotation[5];
        mVec[2] = mRotation[6];
        mVec[3] = 0;
        return mVec;
    }
    /**
     * Gets orientation up vector. Points to temporary storage and will
     * not be valid after call to another getter of {@link GameObj}.  
     * @return
     */
    public float[] getLeftVector() {
        mVec[0] = mRotation[0];
        mVec[1] = mRotation[1];
        mVec[2] = mRotation[2];
        mVec[3] = 0;
        return mVec;
    }
    /**
     * Gets orientation up vector. Points to temporary storage and will
     * not be valid after call to another getter of {@link GameObj}.  
     * @return
     */
    public float[] getForwardVector() {
        mVec[0] = mRotation[8];
        mVec[1] = mRotation[9];
        mVec[2] = mRotation[10];
        mVec[3] = 0;
        return mVec;
    }
    
    /** Returns time of last modification (position, rotation, scale) */
    public float getLastModTime() {
        // if object is dirty then last mod was in current frame,
        // otherwise return time of last transform update
        return mMoved || mRotated || mScaled ? Game.get().getRealTime() 
                : mLastModTime;
    }
    
    /** Indicates if this object is currently active in the engine. */
    public boolean isInitialized() {
        return mIsInitialized;
    }
    
    /**
     * Indicates if the object position or rotation or scale was modified
     * since the last transformation update. When true the new modifications 
     * are not yet reflected in to-world and from-world transformations.
     * @return
     */
    public boolean isDirty() {
        return mMoved || mRotated || mScaled;
    }
    
    private void updateTransform() { 
        MatrixAF.local2World(mToWorld, mPosition, mScale, mRotation);
        mFromWorldValid = false; 
       
        // send events
        final Game game = Game.get();
        if (mMoved && mSendMoveEvent && game != null) {
            final EventManager em = game.getEventManager();
            final IGameObjMoveEvent e = em.obtain(IGameObjMoveEvent.EVENT_TYPE);
            e.setGameObjData(mID, mPosition[0], mPosition[1], mPosition[2]);
            em.queueEvent(e);  
        }
        if (mScaled && mSendScaleEvent && game != null) {
            final EventManager em = game.getEventManager();
            final IGameObjScaleEvent e = em.obtain(IGameObjScaleEvent.EVENT_TYPE);
            e.setGameObjData(mID, mScale[0], mScale[1], mScale[2]);
            em.queueEvent(e);
        }
        if (mRotated && mSendRotateEvent && game != null) {
            final EventManager em = game.getEventManager();
            final IGameObjRotateEvent e = em.obtain(IGameObjRotateEvent.EVENT_TYPE);
            e.setGameObjData(mID);
            em.queueEvent(e);
        }
        mMoved = false;
        mScaled = false;
        mRotated = false;
    }
   
    public float[] getToWorld() {
        return mToWorld;
    }
    
    public float[] getFromWorld() {
        if (!mFromWorldValid && !MatrixAF.invertM(mFromWorld, 0, mToWorld, 0)) {
            throw new IllegalStateException("Inversion of transformation matrix failed.");
        } 
        return mFromWorld;
    }

    @Override
    public boolean mimic(GameObj original) {
        // copy position, rotation, scale and transformations 
        System.arraycopy(original.mPosition, 0, mPosition, 0, 4);
        System.arraycopy(original.mScale, 0, mScale, 0, 4);
        System.arraycopy(original.mRotation, 0, mRotation, 0, 16);
        System.arraycopy(original.mToWorld, 0, mToWorld, 0, 16);
        System.arraycopy(original.mFromWorld, 0, mFromWorld, 0, 16);
    
        mLastModTime    = original.mLastModTime;
        mMoved          = original.mMoved;
        mRotated        = original.mRotated;
        mScaled         = original.mScaled;
        mFromWorldValid = original.mFromWorldValid;
        
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GameObj)) {
            return false;
        }
        final GameObj obj = (GameObj) o;
        return obj.mID == this.mID;
    }

    @Override
    public int hashCode() {
        return mID;
    }

    /**
     * Game object components.
     */
    public interface IComponent {
        ComponentType getComponentType();
        void onUpdate(long realTime, float realDelta, float virtualDelta);
        /**
         * General purpose method callback intended for operations that perform traversals 
         * over all game objects. 
         * @param time Timestamp.
         * @param userData Arbitrary user data.
         */
        void onTraverse(Object userData);
        /** Callback: object was added to game logic */
        void onInit();
        /** Callback: object was removed from game logic */
        void onShutdown();
        /** Resets the component when parent {@link GameObj} is returned to a pool.*/
        void reset();
        
        GameObj getGameObj();
        /** TODO prioC: framework function exposed to users, hide it somehow */ 
        void setGameObj(GameObj obj);
    }
    
    public enum ComponentType {
        // order in this enum also defines order in which components
        // are initialized and updated in GameObject's onUpdate method:
        LIGHT           (true),
        CAMERA          (true),
        LOGIC           (true),
        PHYSICS         (true),
        AI_PASSIVE      (true),
        AI_ACTIVE       (true),  
        BOUNDING_VOLUME (true),
        EFFECT          (false),  // allow multiple effects per object
        MESH            (true),   
        MATERIAL        (true),
        RENDERING       (true),  
        AUDIO           (true),
        SCRATCH_PAD     (true);
        
        private final boolean mUnique;
        ComponentType(boolean isUnique) {
            mUnique = isUnique;
        }
        
        public static int getCount() {
            return ComponentType.values().length;
        }
        
        public int index() {
            return ordinal();
        }
        
        /** 
         * Tells whether or nor a GameObj may have multiple components of
         * this type. 
         * @return
         */
        public boolean isUnique() {
            return mUnique;
        }
        
    }
    
    public static class GameObjLayer { 
        
        private final String mName;
        private final int mMask;
        
        /**
         * Constructs layer. Mask is 32 bit value which defines whether
         * or not the object is sensitive for certain actions. E.g. during 
         * collision checking other objects compare the mask with their own
         * mask via bitwise AND. To make the game object "invisible" to any
         * other object, set mask to 0. 
         * 
         * @param name Layer name.
         * @param mask Mask bits.
         */
        public GameObjLayer(String name, int mask) {
            mName = name;
            mMask = mask;
        }
        
        public String getName() {return mName;}
        public int getMask()    {return mMask;}
        
        /**
         * Tests if this layer mask intersects another layer mask.
         * @param layerMask
         * @return
         */
        public boolean covers(GameObjLayer layerMask) {
            return (mMask & layerMask.mMask) != 0;
        }
        
        /** 
         * Combines masks of this layer and specified one (by OR'ing them) and 
         * returns it as new {@link GameObjLayer} object. 
         * @param other
         * @return
         */
        public GameObjLayer or(GameObjLayer other) {
            String s = mName + "|" + other.mName;
            return new GameObjLayer(s, mMask | other.mMask); 
        }

        
        public boolean equals(GameObjLayer l) {
            return (l != null && this.mMask == l.mMask);
        }
        
    }
 
}
