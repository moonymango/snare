package stachelsau.snare.res.data;

import java.util.ArrayList;

import stachelsau.snare.res.data.MeshResource.BaseImportTransform;

/** Translates mesh so that center of bounding box is located at origin. */
public class ImportTransformCenterToOrigin extends BaseImportTransform {

    private final boolean mSwapYZ;
    private float mBbX;
    private float mBbY;
    private float mBbZ;
    
    /**
     * 
     * @param swapYZ True to also swap Y and Z axis pior to transformation.
     * (y = z, z = -y) 
     */
    public ImportTransformCenterToOrigin(boolean swapYZ) {
        mSwapYZ = swapYZ;
    }
    
private void extractBB(ArrayList<Vertex> vertices) {
        
        final int len = vertices.size();
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        
        for (int i = 0; i < len; i++) {
            final Vertex v = vertices.get(i);
            maxX = v.position.x > maxX ? v.position.x : maxX;
            minX = v.position.x < minX ? v.position.x : minX;
            maxY = v.position.y > maxY ? v.position.y : maxY;
            minY = v.position.y < minY ? v.position.y : minY;
            maxZ = v.position.z > maxZ ? v.position.z : maxZ;
            minZ = v.position.z < minZ ? v.position.z : minZ;
        }
        
        // calculate bb center offset from origin
        float centerX = (maxX + minX)/2;
        float centerY = (maxY + minY)/2;
        float centerZ = (maxZ + minZ)/2;
        
        mBbX = centerX;
        mBbY = centerY;
        mBbZ = centerZ;
    }
    
    
    @Override
    public void transform(ArrayList<Vertex> vertices, ArrayList<Face> faces) 
    {
        extractBB(vertices);
        
        final int len = vertices.size();
        for (int i = 0; i < len; i++)
        {
            final Vertex v = vertices.get(i);
            final float x = v.position.x - mBbX;
            final float y = v.position.y - mBbY;
            final float z = v.position.z - mBbZ;
            
            Vec p;
            if (mSwapYZ) {
                p = new Vec(x, z, -y);
            } else {
                p = new Vec(x, y, z);
            }
            
            v.position = p;
        }
    }
}
