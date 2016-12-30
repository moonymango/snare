package stachelsau.snare.res.data;



public class Face {
    
    protected static final int MAX_VERTEX_CNT = 3;
    
    protected final Vertex[] vertices = new Vertex[MAX_VERTEX_CNT];
    protected Vec normal;
    
    protected Vec getNormal() {
        if (normal == null) {
            final Vec a = vertices[0].position;
            final Vec b = vertices[1].position;
            final Vec c = vertices[2].position;
            final Vec ab = b.subtract(a);
            final Vec ac = c.subtract(a);
            normal = ab.cross(ac); 
        }
        return normal; 
    }
    
    /** 
     * Returns the position of the given vertex in the faces vertex list. 
     * @param v Vertex
     * @return 0, 1 or 2 (for triangles)
     */
    protected int getVertexIndex(Vertex v) {
        for (int i = 0; i < MAX_VERTEX_CNT; i++) {
            if (vertices[i] == v) {
                return i;
            }
        }
        throw new IllegalArgumentException("Face does not contain given vertex.");
    }
    
    protected int getVertexCnt() {
        int result = 0;
        for (int i = vertices.length-1; i >= 0; i--) {
            if (vertices[i] != null) {
                result++;
            }
        }
        return result;
    }
}
