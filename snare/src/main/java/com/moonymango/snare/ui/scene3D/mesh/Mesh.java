package com.moonymango.snare.ui.scene3D.mesh;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.moonymango.snare.res.data.MeshResHandle;
import com.moonymango.snare.res.data.MeshResource;
import com.moonymango.snare.ui.scene3D.BaseMesh;

import android.opengl.GLES20;

/**
 * Mesh which gets its data from the resource cache. 
 * For each mesh data callback the resource handle is obtained and
 * immediately released. So there is no long living handle to a resource.
 */
public class Mesh extends BaseMesh {
    
    private final MeshResource mMeshRes;
    private MeshResHandle mHnd;
    
    public Mesh(MeshResource res) {
        super(res.getQName(), false, false); 
        mMeshRes = res;
    }

    
    @Override
    public void onInit() {
        if (mHnd == null) {         
            mHnd = mMeshRes.getHandle();
        }
        super.onInit();
    }


    @Override
    public void onShutdown() {
        if (mHnd != null) {
            mMeshRes.releaseHandle(mHnd);
            mHnd = null;
        }
        super.onShutdown();
    }


    public boolean hasNormals() { 
        return mHnd.hasNormals();
    }
    
    public int getDrawMode() {
        return GLES20.GL_TRIANGLES;
    }

    public boolean hasTexCoords() {
        return mHnd.hasTexCoords();
    }
  
    public boolean hasColor() {
        // TODO prioC: extend IMeshDataProvider for this
        return false;
    }

    @Override
    protected FloatBuffer getVertices() {
        final FloatBuffer b = mHnd.getVertexAttribs();
        return b;
    }

    @Override
    protected ShortBuffer getIndices() {
        final ShortBuffer b = mHnd.getIndices();
        return b;
    }

    @Override
    protected int getTexOffset() {
        return mHnd.getTexOffset();
    }

    @Override
    protected int getStride() {
        return mHnd.getStride();
    }

    @Override
    protected int getNormalOffset() {
        return mHnd.getNormalOffset();
    }
 
    @Override
    protected int getColorOffset() {
        // TODO prioC: extend IMeshDataProvider for this
        return 0;
    }

    
    @Override
    public int getIndexCount() {
        return mHnd.getIndicesCnt();
    }

    @Override
    public int getIndexOffset() {
        return 0;
    }
    

    /**
     * Provides buffer data and information about the buffer layout and textures.
     */
    public interface IMeshDataProvider {
        /** Return direct buffer with vertex attributes. */
        FloatBuffer getVertexAttribs();
        /** Return direct buffer with indices. */
        ShortBuffer getIndices();
        /** Stride for glAttribVertexPointer. */
        int getStride();
        /** Indicates if mesh provides normals. */
        boolean hasNormals();
        /** Indicates if mesh provides uv info. */
        boolean hasTexCoords();
        /** Offset for glAttribVertexPointer. */
        int getNormalOffset();
        /** Offset for glAttribVertexPointer. */
        int getVertexOffset();
        /** Offset for glAttribVertexPointer. */
        int getTexOffset();
        /** Number of indices for glDrawElements. */
        int getIndicesCnt();
        /** Name of the buffer set (i.e. used as name for VBOs). */
        String getName();
    }
}
