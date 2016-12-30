package stachelsau.snare.ui.scene3D.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import stachelsau.snare.game.Game;
import stachelsau.snare.ui.scene3D.BaseMesh;
import stachelsau.snare.util.Geometry;
import stachelsau.snare.util.VectorAF;
import android.opengl.GLES20;

/**
 * Creates a sphere mesh (or a sector of it) with its center 
 * located at (0, 0, 0) and radius of 1.0.
 */
public class SphereMesh extends BaseMesh {

    private final float mRadius = 1;
    private final int mNumRings;
    private final float mRingHeight;
    private final int mNumSegments;
    private final float mSegmentAngle;
    
    private final int mNumQuads;
    
    private final int mStartSegment;
    private final int mEndSegment;
    private final int mStartRing;
    private final int mEndRing;
        
    /**
     * Creates sector of a sphere (radius 1.0). Horizontal rings are numbered 
     * in ascending order towards negative y axis. Vertical segments are 
     * numbered counter-clockwise around y axis. The total number of quads of the 
     * complete sphere is numRings*numSegments. Note that 
     * the "quads" in first and last latitude section (north and south poles) 
     * are actually triangles.
     * 
     * @param numRings Number of rings of the complete sphere. 
     * @param numSegments Number of segments of complete sphere.
     * @param startRing First ring of mesh. Min=0, max=endRing
     * @param endRing Last ring of mesh. Min=startRing, max=numRings-1
     * @param startSegment First segment of mesh. Min=0, max=endSegment
     * @param endSegment Last segment of mesh. Min=startSegment, max=numSegments-1
     */
    public SphereMesh(int numRings, int numSegments,
            int startRing, int endRing, int startSegment, int endSegment) {
        super(SphereMesh.class.getName() + Game.DELIMITER + numRings 
                + Game.DELIMITER + numSegments + Game.DELIMITER 
                + startRing + Game.DELIMITER + endRing + Game.DELIMITER
                + startSegment + Game.DELIMITER + endSegment,
                false, false);
        if (numSegments < 3 || numRings < 2) {
            throw new IllegalArgumentException(
                    "min. rings = 2, min. segments = 3!");
        }
        
        final boolean valid = startRing <= endRing && startSegment <= endSegment 
                && startRing >= 0 && startSegment >= 0 && endRing < numRings
                && endSegment < numSegments;
        if (!valid) {
            throw new IllegalArgumentException("Invalid sphere clipping values.");
        }
        
        mNumRings = numRings;
        mNumSegments = numSegments;
        mStartSegment = startSegment;
        mEndSegment = endSegment;
        mStartRing = startRing;
        mEndRing = endRing;
        
         
        mNumQuads = (mEndSegment-mStartSegment+1) 
                * (mEndRing-mStartRing+1); 
        
        mRingHeight = 2 * mRadius / mNumRings;
        mSegmentAngle = Geometry.RAD360 / mNumSegments;
    }
    
    /**
     * Creates a sphere with radius 1.0. The total number of quads of the complete sphere
     * is numRings*numSegments. Note that the "quads"
     * in first and last ring (north and south poles) are actually 
     * triangles. North pole of sphere is at (0, 1.0, 0), 
     * southpole at (0, -1.0, 0).
     * 
     * @param numRings Number of horizontal rings.
     * @param numSegments Number of vertical segments.
     */
    public SphereMesh(int numRings, int numSegments) {
        this(numRings, numSegments, 0, numRings-1, 
                0, numSegments-1);
    }
    
    @Override
    public boolean hasNormals() {
        return true;
    }

    @Override
    public boolean hasTexCoords() {
        return false;
    }
    
    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    public int getDrawMode() {
        return GLES20.GL_TRIANGLES;
    }

    @Override
    protected FloatBuffer getVertices() {
        // 4 Vertices per quad, 6 floats per vertex (position + normals)
        final ByteBuffer vbb = ByteBuffer.allocateDirect(mNumQuads * 4 * 6 * Float.SIZE/8); 
        vbb.order(ByteOrder.nativeOrder());
        final FloatBuffer vertexAttribs = vbb.asFloatBuffer();
          
        final float[] upperLeft = new float[4];
        final float[] upperRight = new float[4];
        final float[] lowerLeft = new float[4];
        final float[] lowerRight = new float[4];
        final float[] normal = new float[4];
        
        final float[] arrayA = new float[3*mNumSegments];
        final float[] arrayB = new float[3*mNumSegments];
        float[] upperVertices = arrayA;
        float[] lowerVertices = arrayB;
        
        calcVertices(mStartRing, upperVertices);
        for (int ring = mStartRing; ring <= mEndRing; ring++) {
            calcVertices(ring+1, lowerVertices);
            for (int segment = mStartSegment; segment <= mEndSegment; segment++) {
                // produce a quad 
                // positions
                int i = segment*3;
                upperLeft[0] = upperVertices[i];
                upperLeft[1] = upperVertices[i+1];
                upperLeft[2] = upperVertices[i+2];
                upperLeft[3] = 1;
                
                i = (segment == mNumSegments-1) ? 0 : (segment+1)*3;
                upperRight[0] = upperVertices[i];
                upperRight[1] = upperVertices[i+1];
                upperRight[2] = upperVertices[i+2];
                upperRight[3] = 1;
                
                i = segment*3;
                lowerLeft[0] = lowerVertices[i];
                lowerLeft[1] = lowerVertices[i+1];
                lowerLeft[2] = lowerVertices[i+2];
                lowerLeft[3] = 1;
                
                i = (segment == mNumSegments-1) ? 0 : (segment+1)*3;
                lowerRight[0] = lowerVertices[i];
                lowerRight[1] = lowerVertices[i+1];
                lowerRight[2] = lowerVertices[i+2];
                lowerRight[3] = 1;
                
                // average normals, resulting normal is orthogonal on quad
                normal[0] = upperLeft[0] + upperRight[0] + lowerLeft[0] + lowerRight[0];
                normal[1] = upperLeft[1] + upperRight[1] + lowerLeft[1] + lowerRight[1];
                normal[2] = upperLeft[2] + upperRight[2] + lowerLeft[2] + lowerRight[2];
                normal[3] = 0;
                VectorAF.normalize(normal);
                
                // fill buffer
                vertexAttribs.put(upperLeft[0]);
                vertexAttribs.put(upperLeft[1]);
                vertexAttribs.put(upperLeft[2]);
                vertexAttribs.put(normal[0]);
                vertexAttribs.put(normal[1]);
                vertexAttribs.put(normal[2]);
                
                vertexAttribs.put(lowerLeft[0]);
                vertexAttribs.put(lowerLeft[1]);
                vertexAttribs.put(lowerLeft[2]);
                vertexAttribs.put(normal[0]);
                vertexAttribs.put(normal[1]);
                vertexAttribs.put(normal[2]);
                
                vertexAttribs.put(lowerRight[0]);
                vertexAttribs.put(lowerRight[1]);
                vertexAttribs.put(lowerRight[2]);
                vertexAttribs.put(normal[0]);
                vertexAttribs.put(normal[1]);
                vertexAttribs.put(normal[2]);
                
                vertexAttribs.put(upperRight[0]);
                vertexAttribs.put(upperRight[1]);
                vertexAttribs.put(upperRight[2]);
                vertexAttribs.put(normal[0]);
                vertexAttribs.put(normal[1]);
                vertexAttribs.put(normal[2]);
            }
            
            // switch pointers to reuse the calculated vertices
            // (lower vertices becomes upper vertices of the next ring
            upperVertices = upperVertices == arrayA ? arrayB : arrayA;
            lowerVertices = lowerVertices == arrayA ? arrayB : arrayA;     
        }
          
        vertexAttribs.rewind();
        return vertexAttribs;
    }
    
    @Override
    protected ShortBuffer getIndices() {
        // 2 triangles per quad, 3 indices per triangle
        final ByteBuffer vbb = ByteBuffer.allocateDirect(mNumQuads * 2 * 3 * Short.SIZE/8);  
        vbb.order(ByteOrder.nativeOrder());
        final ShortBuffer indices = vbb.asShortBuffer();  
        
        for (int i = 0; i < mNumQuads; i++) {
            final int offset = i * 4;
            indices.put((short) offset);
            indices.put((short) (offset+1));
            indices.put((short) (offset+2));
            indices.put((short) (offset+2));
            indices.put((short) (offset+3));
            indices.put((short) (offset));
        }
        indices.rewind();
        return indices;
    }
    
    @Override
    protected int getStride() {
        return 6*Float.SIZE/8;
    }

    @Override
    protected int getNormalOffset() {
        return 3*Float.SIZE/8;
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
    public int getIndexCount() {
        // 2 triangles per quad, 3 indices per triangle
        return mNumQuads * 2 * 3;
    }
    
    @Override
    public int getIndexOffset() {
        return 0;
    }
    
    /**
     * Calculates all vertices in a circle around sphere (each ring
     * is defined by two of those circles). The sphere has mNumRings+1 circles. 
     * North pole is first (idx=0), south pole is last (idx=mNumRings). Both pole
     * circles have radius of 0.
     * 
     * @param idx Index of the circle. Min=0, max=mNumRings.
     * @param buf Vertex positions will be filled into this array.
     */
    private void calcVertices(int idx, float[] buf) {
        final float y = mRadius - idx * mRingHeight;
        final float radius = (float) Math.sqrt(mRadius*mRadius - y*y);
        
        for (int i = 0; i < mNumSegments; i++) {
            final float x = (float) (Math.cos(i*mSegmentAngle) * radius);
            final float z = (float) (Math.sin(i*mSegmentAngle) * radius);
            buf[i*3] = x;
            buf[i*3+1] = y;
            buf[i*3+2] = -z; // order segments counter-clockwise around y axis
        }
    }
}
