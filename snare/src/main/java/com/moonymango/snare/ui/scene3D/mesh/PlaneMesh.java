package com.moonymango.snare.ui.scene3D.mesh;

import android.opengl.GLES20;

import com.moonymango.snare.ui.scene3D.BaseMesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Simple plane consisting of 4 vertices.
 */

public class PlaneMesh extends BaseMesh {

    public PlaneMesh() {
        super(PlaneMesh.class.getName(), false, false);
    }

    @Override
    protected FloatBuffer getVertices() {
        // 2 vertices per line, 3 floats per vertex
        final ByteBuffer vbb = ByteBuffer.allocateDirect((mXLines + mZLines) * 2 * 3 * Float.SIZE/8);
        vbb.order(ByteOrder.nativeOrder());
        final FloatBuffer vertexAttribs = vbb.asFloatBuffer();

        // lines parallel to z axis
        float xPos = -mMaxX;
        final float xMax = mMaxX + mSquare/2;
        while(xPos < xMax) {
            vertexAttribs.put(xPos);
            vertexAttribs.put(0);
            vertexAttribs.put(-mMaxZ);
            vertexAttribs.put(xPos);
            vertexAttribs.put(0);
            vertexAttribs.put(mMaxZ);
            xPos += mSquare;
        }

        // lines parallel to x axis
        float zPos = -mMaxZ;
        final float zMax = mMaxZ + mSquare/2;
        while(zPos < zMax) {
            vertexAttribs.put(-mMaxX);
            vertexAttribs.put(0);
            vertexAttribs.put(zPos);
            vertexAttribs.put(mMaxX);
            vertexAttribs.put(0);
            vertexAttribs.put(zPos);
            zPos += mSquare;
        }

        vertexAttribs.rewind();
        return vertexAttribs;
    }

    @Override
    protected ShortBuffer getIndices() {
        return null;
    }

    @Override
    protected int getStride() {
        return 0;
    }

    @Override
    protected int getNormalOffset() {
        return 0;
    }

    @Override
    protected int getTexOffset() {
        return 0;
    }

    @Override
    protected int getColorOffset() {
        return 0;
    }

    @Override
    public boolean hasNormals() {
        return true;
    }

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public boolean hasTexCoords() {
        return true;
    }

    @Override
    public int getDrawMode() {
        return GLES20.GL_TRIANGLES;
    }

    @Override
    public int getIndexCount() {
        return 0;
    }

    @Override
    public int getIndexOffset() {
        return 0;
    }
}
