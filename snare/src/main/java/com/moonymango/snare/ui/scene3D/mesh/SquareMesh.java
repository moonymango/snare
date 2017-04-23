package com.moonymango.snare.ui.scene3D.mesh;

import android.opengl.GLES20;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.physics.IBoundingVolumeProvider;
import com.moonymango.snare.ui.scene3D.BaseMesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Generates a square of 4 vertices in x-z plane at coordinates (-1,-1), (-1,1), (1,1), (1,-1).
 * Normal vector is (0, 1, 0).
 */

public class SquareMesh extends BaseMesh implements IBoundingVolumeProvider
{
    public SquareMesh(IGame game) {
        super(game, SquareMesh.class.getName(), false, false);
    }

    @Override
    public int getDrawMode() {
        return GLES20.GL_TRIANGLES;
    }

    @Override
    protected int getColorOffset() {
        return 0;
    }

    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public int getIndexCount() {
        // 2 triangles, 3 vertices per triangle
        return 6;
    }

    @Override
    public int getIndexOffset() {
        return 0;
    }

    @Override
    protected ShortBuffer getIndices() {
        // 2 triangles, 3 indices per triangle
        final ByteBuffer vbb = ByteBuffer.allocateDirect(2 * 3 * Short.SIZE/8);
        vbb.order(ByteOrder.nativeOrder());
        final ShortBuffer indices = vbb.asShortBuffer();

        indices.put((short) 0);
        indices.put((short) 1);
        indices.put((short) 2);
        indices.put((short) 2);
        indices.put((short) 3);
        indices.put((short) 0);

        indices.rewind();
        return indices;
    }

    @Override
    protected int getNormalOffset() {
        return 3*Float.SIZE/8;
    }

    @Override
    protected int getStride() {
        return 8*Float.SIZE/8;
    }

    @Override
    protected int getTexOffset() {
        return 6*Float.SIZE/8;
    }

    @Override
    protected FloatBuffer getVertices() {
        // 4 Vertices per quad, 8 floats per vertex (position + normals + tex coords)
        final ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 8 * Float.SIZE/8);
        vbb.order(ByteOrder.nativeOrder());
        final FloatBuffer vertexAttribs = vbb.asFloatBuffer();

        vertexAttribs.put(-1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(-1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(0f);
        vertexAttribs.put(0f);

        vertexAttribs.put(-1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);

        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(1f);

        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(-1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);
        vertexAttribs.put(1f);
        vertexAttribs.put(0f);

        vertexAttribs.rewind();
        return vertexAttribs;
    }

    @Override
    public boolean hasNormals() {
        return true;
    }

    @Override
    public boolean hasTexCoords() {
        return true;
    }

    @Override
    public float getMaxX()
    {
        return 1;
    }

    @Override
    public float getMinX()
    {
        return -1;
    }

    @Override
    public float getMaxY()
    {
        return 0;
    }

    @Override
    public float getMinY()
    {
        return 0;
    }

    @Override
    public float getMaxZ()
    {
        return 1;
    }

    @Override
    public float getMinZ()
    {
        return -1;
    }
}
