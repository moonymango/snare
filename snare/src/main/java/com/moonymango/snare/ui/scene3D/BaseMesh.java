package com.moonymango.snare.ui.scene3D;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_DYNAMIC_DRAW;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.moonymango.snare.game.GameObj.ComponentType;
import com.moonymango.snare.game.logic.BaseComponent;
import com.moonymango.snare.opengl.BufferObj;
import com.moonymango.snare.opengl.BufferObj.IBufferConfigurationSetup;
import com.moonymango.snare.opengl.BufferObj.IBufferDataProvider;
import com.moonymango.snare.opengl.BufferObj.IBufferUpdateSetup;
import com.moonymango.snare.opengl.BufferObj.Target;
import com.moonymango.snare.opengl.GLObjDescriptor;
import com.moonymango.snare.opengl.GLObjDescriptor.GLObjType;

public abstract class BaseMesh extends BaseComponent 
        implements IBufferDataProvider {

    private final GLObjDescriptor mVertexBufferDescr;
    private BufferObj mVertexBufferObj;
    private final GLObjDescriptor mIndexBufferDescr;
    private BufferObj mIndexBufferObj;
    
    private int mStride;
    private int mNormalOffset;
    private int mTexOffset;
    private int mColorOffset;
    
    private final boolean mUpdatableVertices;
    private final boolean mUpdatableIndices;
    
    private int mHash;
    
    /**
     * Constructs a mesh object
     * @param name Base name used for all the mesh's GL objects (buffers, textures).
     * @param updatableVertices True to allow updates to vertex buffer data. 
     * @param updatableIndices True to allow updates to index buffer data.
     */
    protected BaseMesh(String name, boolean updatableVertices,
            boolean updatableIndices) {
        super(ComponentType.MESH);
        mVertexBufferDescr = new GLObjDescriptor(name + ".vertices", GLObjType.BUFFER);
        mIndexBufferDescr = new GLObjDescriptor(name + ".indices", GLObjType.BUFFER);
        mUpdatableVertices = updatableVertices;
        mUpdatableIndices = updatableIndices;
    }
    
    /**
     * Updates vertex and/or index buffer data. Must only be called
     * after configureBuffers()!
     * Note: Update is only possible if the mesh was created with
     * update flags set to true and callback functions
     * getVertices(), getIndices(), getUpdateOffset() and getUpdateSize()
     * implemented accordingly. This method only marks the the
     * respective buffers for update, the actual update is done in
     * GL thread. So calling this multiple times in a frame has no
     * performance impact.
     * @param num
     */
    protected void updateVertices(int num) {
        if (mUpdatableVertices) {
            mVertexBufferObj.update(num);
        }
    }
    protected void updateIndices(int num) {
        if (mUpdatableIndices) {
            mIndexBufferObj.update(num);
        }
    }
        
    protected void loadToGpu() {
        // set up VBOs
        mStride = getStride();
        mNormalOffset = getNormalOffset();
        mTexOffset = getTexOffset();
        mColorOffset = getColorOffset();
        
        if (mVertexBufferObj == null) {
            mVertexBufferObj = (BufferObj) mVertexBufferDescr.getHandle();
        }
        if (!mVertexBufferObj.isConfigured()) {
            mVertexBufferObj.configure(this);
        }
        
        if (mIndexBufferObj == null) {
            mIndexBufferObj = (BufferObj) mIndexBufferDescr.getHandle();
        }
        if (!mIndexBufferObj.isConfigured()) {
            mIndexBufferObj.configure(this);
        }
        
        // calc hash based on vertex attributes
        final int prime = 31;
        mHash = 1;
        final String q = mVertexBufferDescr.getQName();
        mHash = prime * mHash + q.hashCode();   
        
    }

    protected void unloadFromGpu() {
        if (mVertexBufferObj != null) {
            mVertexBufferDescr.releaseHandle(mVertexBufferObj);
            mVertexBufferObj = null;
        }
        if (mIndexBufferObj != null) {
            mIndexBufferDescr.releaseHandle(mIndexBufferObj);
            mIndexBufferObj = null;
        }
    }
    
    /**
     * Returns vertex data when vertex buffer is to be created.
     * @return
     */
    protected abstract FloatBuffer getVertices();
    /**
     * Returns indices when index buffer is to be created.
     * @return
     */
    protected abstract ShortBuffer getIndices();
    /** Returns stride in bytes. Called after configureBuffers() */
    protected abstract int getStride();
    /** Returns offset of normals in bytes. Called after configureBuffers() */
    protected abstract int getNormalOffset();
    /** Returns offset of tex coords in bytes. Called after configureBuffers() */
    protected abstract int getTexOffset();
    /** Returns offset of color data in bytes. Called after configureBuffers() */
    protected abstract int getColorOffset();
    
    public void bindBuffers(int positionLocation, int normalLocation,
            int textureLocation, int colorLocation) {
        glBindBuffer(GL_ARRAY_BUFFER, mVertexBufferObj.getID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferObj.getID());
        if (positionLocation >= 0) {
            // floats per vertex
            glVertexAttribPointer(positionLocation, 3, 
                    GL_FLOAT, false, mStride, 0);
            glEnableVertexAttribArray(positionLocation);
        }
        
        if (normalLocation >= 0) {
            // 3 floats per normal
            glVertexAttribPointer(normalLocation, 3, 
                    GL_FLOAT, false, mStride, mNormalOffset);
            glEnableVertexAttribArray(normalLocation);
        }
        
        if (colorLocation >= 0) {
            // 4 floats per color
            glVertexAttribPointer(colorLocation, 4, 
                    GL_FLOAT, false, mStride, mColorOffset);
            glEnableVertexAttribArray(colorLocation);
        }
        
        if (textureLocation >= 0) {
            // 2 floats per tex coord
            glVertexAttribPointer(textureLocation, 2, 
                    GL_FLOAT, false, mStride, mTexOffset);
            glEnableVertexAttribArray(textureLocation);
        }
    }
   
    public Material getMaterial() {
        return null;
    }

    @Override
    public final void getConfigurationSetup(String name,
            IBufferConfigurationSetup setup) {
        
        if (name.equals(mVertexBufferDescr.getQName())) {
            setup.setTarget(Target.ARRAY);
            setup.enableAutoSize(true);
            setup.setUsage(mUpdatableVertices ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
            setup.setBuffer(getVertices());
            return;
        }
        if (name.equals(mIndexBufferDescr.getQName())) {
            setup.setTarget(Target.ELEMENT);
            setup.enableAutoSize(true);
            setup.setUsage(mUpdatableIndices ? GL_DYNAMIC_DRAW : GL_STATIC_DRAW);
            setup.setBuffer(getIndices());
            return;
        }
        throw new IllegalArgumentException("Unkown descriptor name.");
        
    }
    
    @Override
    public final void getUpdateSetup(String name, int pass, IBufferUpdateSetup setup) {
       if (name.equals(mVertexBufferDescr.getQName())) {
            getUpdateSetup(false, setup, pass);
            return;
        }
        if (name.equals(mIndexBufferDescr.getQName())) {
            getUpdateSetup(true, setup, pass);
            return;
        }
        throw new IllegalArgumentException("Unkown descriptor name.");
    }
    
    /**
     * Collects update setup. 
     * @param indices True for index buffer update, false for vertex buffer.
     * @param setup
     * @param pass
     */
    protected void getUpdateSetup(boolean indices, IBufferUpdateSetup setup, 
            int pass) {
        throw new UnsupportedOperationException("Update not supported.");
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BaseMesh other = (BaseMesh) obj;
         return mHash == other.mHash;
    }
    
    public static final int UNUSED_LOCATION = -1;
    /** True, if array buffer contains normals. */
    public abstract boolean hasNormals();
    /** True, if array buffer contains color information. */
    public abstract boolean hasColor();
    /** True if array buffer contains texture coords. */
    public abstract boolean hasTexCoords();
    /** Returns GL draw mode this mesh is intended for. */
    public abstract int getDrawMode();
    /**
     * Sets up vertex array pointers for the given locations.
     * Use constant UNUSED_LOCATION on unused locations.
     * Note: glDisableVertexAttribArray may be called manually after 
     * drawing. 
     * @param positionLocation 
     * @param normalLocation
     * @param textureLocation
     * @param colorLocation
     */
    //public abstract void bindBuffers(int positionLocation, int normalLocation, 
      //      int textureLocation, int colorLocation);
    /** Returns number of indices to draw. */
    public abstract int getIndexCount();
    /** 
     * Returns offset into index buffer object. 
     * Note: must be multiplied with size of type to be applicable in glDrawElements.
     */
    public abstract int getIndexOffset();
    /** Returns material in case the mesh provides one. */
    //public abstract Material getMaterial();
}
