package stachelsau.snare.res.data;

import java.util.ArrayList;

import stachelsau.snare.res.data.MeshResource.BaseImportTransform;
import stachelsau.snare.util.MatrixAF;

/** Applies a custom transformation matrix. */
public class ImportTransformCustom extends BaseImportTransform {

    private final float[] mT = new float[16];
    private final float[] mVec = new float[4];
    private final boolean mSwapYZ;
    
    /**
     * 
     * @param mat Transformation matrix.
     * @param swapYZ Swap y and z axis prior to transformation.
     * (y = z, z = -y)
     */
    public ImportTransformCustom(float[] mat, boolean swapYZ) {
        for (int i = 0; i < 16; i++) {
            mT[i] = mat[i];
        }
        mSwapYZ = swapYZ;
    }

    @Override
    public void transform(ArrayList<Vertex> vertices, ArrayList<Face> faces) 
    {
        final int len = vertices.size();
        for (int i = 0; i < len; i++)
        {
            final Vertex v = vertices.get(i);
            mVec[0] = v.position.x;
            mVec[1] = mSwapYZ ? v.position.z : v.position.y;
            mVec[2] = mSwapYZ ? -v.position.y : v.position.z;
            mVec[3] = 1;
            
            MatrixAF.multiplyMV(mT, mVec);
            v.position = new Vec(mVec[0], mVec[1], mVec[2]);
        }
    }

}
