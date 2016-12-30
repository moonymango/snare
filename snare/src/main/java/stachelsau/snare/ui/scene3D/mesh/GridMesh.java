package stachelsau.snare.ui.scene3D.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

import stachelsau.snare.game.Game;
import stachelsau.snare.opengl.TextureObjOptions;
import stachelsau.snare.res.texture.BaseTextureResource;
import stachelsau.snare.ui.scene3D.BaseMesh;

/**
 * Generates a mesh of sqares centered in the xz plane. Intended to be drawn with
 * GL_LINES.
 */
public class GridMesh extends BaseMesh {
    
    private final float mSquare = 1;
    private final int mXLines;
    private final int mZLines;
    private final float mMaxX;
    private final float mMaxZ;
    private int mIndexCnt;
    
    /**
     * @param x Number of squares in x dimension.
     * @param z Number of squares in z dimension.
     */
    public GridMesh(int x, int z) {
        super(GridMesh.class.getName() + Game.DELIMITER + x + Game.DELIMITER + z,
                false, false);
        mXLines = x + 1;
        mZLines = z + 1;
        mMaxX = mSquare * x /2;
        mMaxZ = mSquare * z /2;
        mIndexCnt = (mXLines + mZLines) * 2; // 2 indices per line
    }
    
    public boolean hasTexCoords() {
        return false;
    }

    @Override
    public boolean hasColor() {
        return false;
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
    
    public ShortBuffer getIndices() {
        // 2 indices per line
        final ByteBuffer vbb = ByteBuffer.allocateDirect((mXLines + mZLines) * 2 * Short.SIZE/8);  
        vbb.order(ByteOrder.nativeOrder());
        final ShortBuffer indices = vbb.asShortBuffer();  
        
        final int len = indices.capacity();
        for (int i = 0; i < len; i++) {
            indices.put((short) i);
        }
        indices.rewind();
        return indices;
    }

    public boolean hasNormals() { 
        // vertices only
        return false;
    }    
        
    public int getStride() {
        return 3 * Float.SIZE/8;
    }

    public int getNormalOffset() {
        return 0;
    }

    public int getIndexCount() {
        return mIndexCnt;
    }

    protected int getTexOffset() {
        return 0;
    }
    
    @Override
    protected int getColorOffset() {
        return 0;
    }

    public int getTextureCount() {
        return 0;
    }

    public BaseTextureResource getTexture(int idx) {
        return null;
    }
    
    public TextureObjOptions getTextureOptions(int idx) {
        return null;
    }

    public int getDrawMode() {
        return GLES20.GL_LINES;
    }

    @Override
    public int getIndexOffset() {
        return 0;
    }
    
}
