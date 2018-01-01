package com.moonymango.snare.res.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Code in this file was taken and adapted from min3d loader classes.
 * See Min3D on github: https://github.com/mengdd/min3d
 *
 *****************************************************************
 * Project homepage: 	http://code.google.com/p/min3d
 * License:				MIT
 * 
 * Author: 				Lee Felarca
 * Website:				http://www.zeropointnine.com/blog
 *
 * Author: 				Dennis Ippel
 * Author blog:			http://www.rozengain.com/blog/
 *****************************************************************
 */


/* FIXME prioD: handle multiple objects in one file (see mEnabled)
 * TODO prioD: handle materials */
public class Max3DSParser extends BaseMeshParser {

   
    private static final int IDENTIFIER_3DS = 0x4D4D;
    private static final int MESH_BLOCK = 0x3D3D;
    private static final int OBJECT_BLOCK = 0x4000;
    private static final int TRIMESH = 0x4100;
    private static final int TRI_MATERIAL = 0x4130;
    private static final int VERTICES = 0x4110;
    private static final int FACES = 0x4120;
    private static final int TEXCOORD = 0x4140;
    private static final int TEX_MAP = 0xA200;
    private static final int TEX_NAME = 0xA000;
    private static final int TEX_FILENAME = 0xA300;
    private static final int MATERIAL = 0xAFFF;

    private int chunkID;
    private int chunkEndOffset;
    private boolean endReached;
    private String currentObjName;
    
    private String mMeshName;
    private boolean mEnabled;
    
    private int mNumVertices;
    private int mNumTriangles;
        
    private final ArrayList<Vertex> mVertexData = new ArrayList<Vertex>();
    private final ArrayList<Face> mFaces = new ArrayList<Face>();
    
    
    @Override
    protected boolean parse(InputStream in, String name) throws IOException {
        mMeshName = name;
        readHeader(in);
        if(chunkID != IDENTIFIER_3DS) {
            return false;
        }
        while(!endReached) {
            readChunk(in);
        } 
        
        return true;
    }
        
    private void readHeader(InputStream stream) throws IOException {
        chunkID = readShort(stream);
        chunkEndOffset = readInt(stream);
        endReached = chunkID < 0;
    }
        
    private void readChunk(InputStream stream) throws IOException {
        readHeader(stream);
        
        switch (chunkID) {
       
        case VERTICES:
                readVertices(stream);
                break;
        case FACES:
                readFaces(stream);
                break;
                
        case MESH_BLOCK:
                break;
            
        case OBJECT_BLOCK:
                currentObjName = readString(stream);
                break;
                
        case TRIMESH:
                mEnabled = !mEnabled && mMeshName.equals(currentObjName);
                break;
                
        case TEXCOORD:
                readTexCoords(stream);
                break;
                
        case TEX_NAME:
                //String tmp = readString(stream);
                readString(stream);
                break;
                
        case TEX_FILENAME:
                //String fileName = readString(stream);
                readString(stream);
                break;
                
        case TRI_MATERIAL:
                //String materialName = readString(stream);
                readString(stream);
                int numFaces = readShort(stream);
                for(int i=0; i<numFaces; i++) {
                        //int faceIndex = readShort(stream);
                        readShort(stream);
                }
                break;
        case MATERIAL:
                break;
        case TEX_MAP:
                break;
        default:
                skipRead(stream);
        }
    }
        
    private void skipRead(InputStream stream) throws IOException {
        for(int i=0; (i < chunkEndOffset - 6) && !endReached; i++) {
                endReached = stream.read() < 0;
        }
    }
        
    private void readVertices(InputStream buffer) throws IOException {
        mNumVertices = readShort(buffer);
        for (int i = 0; i < mNumVertices; i++) {
            final float x = readFloat(buffer);
            final float y = readFloat(buffer);
            final float z = readFloat(buffer);
                    
            Vertex v = new Vertex();
            //v.position = new Vec(x, z, -y);
            v.position = new Vec(x, y, z);
            v.index = i;
            mVertexData.add(v);
        }
    }
        
    private void readFaces(InputStream buffer) throws IOException {
        mNumTriangles = readShort(buffer);
        for (int i = 0; i < mNumTriangles; i++) {
            final short a = (short) readShort(buffer);
            final short b = (short) readShort(buffer);
            final short c = (short) readShort(buffer);
            readShort(buffer);
            
            final Face face = new Face();
            Vertex vertex = mVertexData.get(a);
            face.vertices[0] = vertex;
            vertex.faces.add(face);
            
            vertex = mVertexData.get(b);
            face.vertices[1] = vertex;
            vertex.faces.add(face);
            
            vertex = mVertexData.get(c);
            face.vertices[2] = vertex;
            vertex.faces.add(face);
            
            mFaces.add(face);
        }
    }
    
    private void readTexCoords(InputStream buffer) throws IOException {
        int numVertices = readShort(buffer);
        if (numVertices != mNumVertices)
        {
            throw new IOException("Number of texture coordinates is different " +
            		"from number of vertices.");
        }
        for (int i = 0; i < numVertices; i++) {
            // add tex coord info to corresponding vertex
            final float u = readFloat(buffer);
            final float v = readFloat(buffer);
            final Vertex vertex = mVertexData.get(i);
            vertex.uv = new Vec(u, v, 0);
        }
    }
  
    @Override
    protected ArrayList<Vertex> getVertexData() {
        return mVertexData;
    }

    @Override
    protected ArrayList<Face> getFaces() {
        return mFaces;
    }


}
