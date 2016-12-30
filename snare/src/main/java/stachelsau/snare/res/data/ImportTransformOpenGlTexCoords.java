package stachelsau.snare.res.data;

import java.util.ArrayList;

import stachelsau.snare.res.data.MeshResource.BaseImportTransform;

/**
 * Transforms uv information into OpenGL texture coordinates, i.e.
 * it negates the v value.
 */
public class ImportTransformOpenGlTexCoords extends BaseImportTransform {

    @Override
    void transform(ArrayList<Vertex> vertices, ArrayList<Face> faces) {
        final int len = vertices.size();
        for (int i = 0; i < len; i++)
        {
            final Vertex v = vertices.get(i);
            if (v.uv != null) {
                final float x = v.uv.x;
                final float y = -v.uv.y;
                v.uv = new Vec(x, y, 0);
            }
        }
    }

}
