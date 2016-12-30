package stachelsau.snare.res.data;

import java.util.ArrayList;

public class Vertex {
    
    public Vec position;
    public Vec normal;
    public Vec uv;      // only x and y component used for tex coordinates
    /** The index used by index buffer to reference this vertex.*/
    protected int index;
    /** List of all faces this vertex is a member of. */ 
    protected final ArrayList<Face> faces = new ArrayList<Face>();

}
