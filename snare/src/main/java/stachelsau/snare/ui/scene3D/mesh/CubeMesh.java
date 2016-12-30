package stachelsau.snare.ui.scene3D.mesh;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import stachelsau.snare.ui.scene3D.BaseMesh;

public class CubeMesh extends BaseMesh {

    
    public CubeMesh() {
        super(CubeMesh.class.getName(), false, false);
    }

    @Override
    protected FloatBuffer getVertices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected ShortBuffer getIndices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected int getStride() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int getNormalOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int getTexOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected int getColorOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasNormals() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasColor() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasTexCoords() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getDrawMode() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIndexCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getIndexOffset() {
        // TODO Auto-generated method stub
        return 0;
    }

}
