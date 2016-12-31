package com.moonymango.snare.res.data;

import java.util.ArrayList;

import com.moonymango.snare.res.data.MeshResource.BaseImportTransform;

/**
 * Transform for Blender generated files:
 *  - swaps y and z axis to adjust to OpenGl coordinate system
 *  - multiply v coordinate by -1 to adjust for OpenGl texture coordinates  
 */
public class ImportTransformBlender extends BaseImportTransform {

    @Override
    public void transform(ArrayList<Vertex> vertices, ArrayList<Face> faces) {
        for (int i = 0; i < vertices.size(); i++)
        {
            final Vertex v = vertices.get(i);
            final float x = v.position.x;
            final float y = v.position.y;
            final float z = v.position.z;
            v.position = new Vec(x, z, -y);
            
            if (v.uv != null) {
                final float s = v.uv.x;
                final float t = -v.uv.y;
                v.uv = new Vec(s, t, 0);
            }
        }
        
    }

}
