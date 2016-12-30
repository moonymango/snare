package stachelsau.snare.res.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public abstract class BaseMeshParser {
    
    protected abstract boolean parse(InputStream in, String name) throws IOException;
    protected abstract ArrayList<Vertex> getVertexData();
    protected abstract ArrayList<Face> getFaces();
      
    protected String readString(InputStream stream) throws IOException {
        String result = new String();
        byte inByte;
        while ((inByte = (byte) stream.read()) != 0)
                result += (char) inByte;
        return result;
    }

    protected int readInt(InputStream stream) throws IOException {
        return stream.read() | (stream.read() << 8) | (stream.read() << 16) 
                | (stream.read() << 24);
    }

    protected int readShort(InputStream stream) throws IOException {
            return (stream.read() | (stream.read() << 8));
    }
    
    protected float readFloat(InputStream stream) throws IOException {
            return Float.intBitsToFloat(readInt(stream));
    }
     
}
