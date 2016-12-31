package com.moonymango.snare.res.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import com.moonymango.snare.physics.IBoundingVolumeProvider;
import com.moonymango.snare.res.BaseResHandle;
import com.moonymango.snare.res.data.MeshResource.BaseImportTransform;
import com.moonymango.snare.ui.scene3D.mesh.Mesh.IMeshDataProvider;

public class MeshResHandle extends BaseResHandle implements IBoundingVolumeProvider,
        IMeshDataProvider {

    private static final float SMOOTH_ANGLE_GRAD = 80; 
    private static final float SMOOTH_ANGLE_COS = (float) Math.cos((float) (2*Math.PI/360*SMOOTH_ANGLE_GRAD));
    private static final int FLOATS_PER_VERTEX = 6;    // 3x position, 3x normal
    private static final int FLOATS_PER_VERTEX_UV = 8; // 3x pos, 3x normal, 2x uv
    private static final int SHORTS_PER_FACE = 3;      // 3 indices per triangle 
    
    private FloatBuffer mVertexAttribs;
    private ShortBuffer mIndices; 
    private ArrayList<Vertex> mVertexData;
    private ArrayList<Face> mFaces;
    private final boolean mHasNormals;
    private final boolean mHasTexCoords;
    
    private float mMinX;
    private float mMaxX;
    private float mMinY;
    private float mMaxY;
    private float mMinZ;
    private float mMaxZ;
    
    
    public MeshResHandle(MeshResource res, BaseMeshParser parser, 
            BaseImportTransform transform) {
        super(res);
        if (parser == null) {
            throw new IllegalArgumentException("Missing mesh parser.");
        }
        
        // get imported raw data
        mVertexData = parser.getVertexData();
        mFaces = parser.getFaces();
        
        if (mVertexData == null || mFaces == null) {
            throw new IllegalStateException("Missing vertex positions or faces.");
        }
        
        // apply import transformations
        BaseImportTransform t = transform;
        while (t != null)
        {
            t.transform(mVertexData, mFaces);
            t = t.getNext();
        }
            
        
        // calculate normals in case there are none given with vertices
        // (just test first vertex in array if it has normals)
        if (mVertexData.get(0).normal == null) {
            calculateNormals();
        } 
        mHasNormals = true;
        
        // check if uv information is there
        mHasTexCoords = mVertexData.get(0).uv != null;
        
        // set up deliveries of this handle
        buildVertexAttrBuffer();
        buildIndexBuffer();
        extractBoundingBox();
        
        // clean up
        mVertexData = null;
        mFaces = null;
    }

      
    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getVertexAttribs()
     */
    public FloatBuffer getVertexAttribs() {
        return mVertexAttribs;
    }
    
    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getIndices()
     */
    public ShortBuffer getIndices() {
        return mIndices;
    }
    
    
    @Override
    public boolean hasNormals() {
        return mHasNormals;
    }


    @Override
    public boolean hasTexCoords() {
        return mHasTexCoords;
    }


    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getStride()
     */
    public int getStride() {
        final int fpv = mHasTexCoords ? FLOATS_PER_VERTEX_UV : FLOATS_PER_VERTEX;
        return fpv * Float.SIZE/8;
    }
    
    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getNormalOffset()
     */
    public int getNormalOffset() {
        // 3 floats for position before normal
        return 3*Float.SIZE/8;
    }
    
    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getVertexOffset()
     */
    public int getVertexOffset() {
        return 0;
    }
    
    public int getTexOffset() {
        // 6 floats for position and normal before uv info
        return mHasTexCoords ? 6*Float.SIZE/8 : 0;
    }


    /* (non-Javadoc)
     * @see com.moonymango.snare.res.loader3d.IBufferProvider#getIndicesCnt()
     */
    public int getIndicesCnt() {
        return mIndices.capacity();
    }
    
    public String getName() {
        // use name of mesh resource
        return mDescriptor.getQName();
    }


    private void calculateNormals() {
        splitFacesToTriangles();
        
        ArrayList<Face> tmpFaces = new ArrayList<Face>();
        
        // calculate normal for every vertex
        for (int vi = mVertexData.size() - 1; vi >= 0; vi--) {     
            // fill all faces that contain this vertex into tmpFaces
            final Vertex vOrigin = mVertexData.get(vi);
            tmpFaces.clear();
            tmpFaces.addAll(vOrigin.faces);
            
            // Assign an averaged normal to the vertex based on face area to smooth surface.
            // If faces form edges/angles less than MIN_SMOOTH_ANGLE they will start a new 
            // smoothing group to preserve the edge an a new vertex is created.
            Vertex vActual = vOrigin;
            while (tmpFaces.size() > 0) {
                Vec avgNormal = null;
                
                for (int fi = tmpFaces.size() - 1; fi >= 0; fi--) {
                    final Face face = tmpFaces.get(fi);
                    final Vec faceNormal = face.getNormal();
                    
                    if (avgNormal != null) {
                        final float cosOfAngle = avgNormal.normalize().dot(faceNormal.normalize());
                        if (cosOfAngle < SMOOTH_ANGLE_COS) {
                            // preserve the edge and do not include this face in the
                            // averaged normal. The face will be handled in subsequent loops.
                            continue;
                        } 
                        // include face's normal in averaged normal
                        avgNormal = avgNormal.add(faceNormal);
                        
                    } else {
                        avgNormal = new Vec(faceNormal);
                    }
                    
                    // remove face from todo list and update vertex reference
                    tmpFaces.remove(fi);
                    if (vActual != vOrigin) {
                        // replace vertex at exactly position same position to maintain 
                        // the face's orientation
                        final int idx = face.getVertexIndex(vOrigin);
                        face.vertices[idx] = vActual;
                    }
                }
                
                // add actual vertex and create new one for next smoothing group
                vActual.normal = avgNormal.normalize();
                if (vActual != vOrigin) {
                    mVertexData.add(vActual);
                }
                vActual = new Vertex();
                vActual.position = new Vec(vOrigin.position);
                vActual.uv = vOrigin.uv != null ? new Vec(vOrigin.uv) : null;
                vActual.index = mVertexData.size();
            }       
        }
    }
    
    private void extractBoundingBox() {
        final int len = mVertexData.size();
        mMaxX = -Float.MAX_VALUE;
        mMaxY = -Float.MAX_VALUE;
        mMaxZ = -Float.MAX_VALUE;
        mMinX = Float.MAX_VALUE;
        mMinY = Float.MAX_VALUE;
        mMinZ = Float.MAX_VALUE;
        
        for (int i = 0; i < len; i++) {
            final Vertex v = mVertexData.get(i);
            mMaxX = v.position.x > mMaxX ? v.position.x : mMaxX;
            mMinX = v.position.x < mMinX ? v.position.x : mMinX;
            mMaxY = v.position.y > mMaxY ? v.position.y : mMaxY;
            mMinY = v.position.y < mMinY ? v.position.y : mMinY;
            mMaxZ = v.position.z > mMaxZ ? v.position.z : mMaxZ;
            mMinZ = v.position.z < mMinZ ? v.position.z : mMinZ;
        }
        
    }
    
    private void buildVertexAttrBuffer() {
        final int len = mVertexData.size();
        final int floatPerVertex = mHasTexCoords ? 
                FLOATS_PER_VERTEX_UV : FLOATS_PER_VERTEX; 
                    
        final ByteBuffer vbb = ByteBuffer.allocateDirect(len * floatPerVertex * Float.SIZE/8 ); 
        vbb.order(ByteOrder.nativeOrder());
        mVertexAttribs = vbb.asFloatBuffer();
        
        for (int i = 0; i < len; i++) {
            final Vertex v = mVertexData.get(i);
            mVertexAttribs.put(v.position.x);
            mVertexAttribs.put(v.position.y);
            mVertexAttribs.put(v.position.z);
            mVertexAttribs.put(v.normal.x);
            mVertexAttribs.put(v.normal.y);
            mVertexAttribs.put(v.normal.z);
            
            if (mHasTexCoords) {
                    mVertexAttribs.put(v.uv.x);
                    mVertexAttribs.put(v.uv.y);
            }
        }
        mVertexAttribs.rewind();
    }
    
    private void buildIndexBuffer() {
        final int len = mFaces.size();
        final ByteBuffer vbb = ByteBuffer.allocateDirect(len * SHORTS_PER_FACE * Short.SIZE/8);  
        vbb.order(ByteOrder.nativeOrder());
        mIndices = vbb.asShortBuffer();  
        
        for (int i = 0; i < len; i++) {
            final Face f = mFaces.get(i);
            mIndices.put((short) f.vertices[0].index);
            mIndices.put((short) f.vertices[1].index);
            mIndices.put((short) f.vertices[2].index);
        }
        mIndices.rewind();
    }
    
    private void splitFacesToTriangles() {
        // TODO prioC: Extend to polygons
        // at the moment we just check that only triangles are in the faces list.
        for (int i=mFaces.size()-1; i>=0; i--) {
            if (mFaces.get(i).getVertexCnt() != 3) {
                throw new UnsupportedOperationException("Only triangular faces supported.");
            }
        }
    }


    public float getMaxX() {return mMaxX;}
    public float getMinX() {return mMinX;}
    public float getMaxY() {return mMaxY;}
    public float getMinY() {return mMinY;}
    public float getMaxZ() {return mMaxZ;}
    public float getMinZ() {return mMinZ;}

    
}
