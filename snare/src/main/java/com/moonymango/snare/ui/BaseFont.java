package com.moonymango.snare.ui;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.moonymango.snare.res.IAssetName;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.ETC1TextureResource;

/**
 * TODO prioC: derive {@link BaseFont} directly from {@link BaseTextureResource}
 * so that {@link ETC1TextureResource} can also be used
 */
public abstract class BaseFont extends BitmapTextureResource {

    // buffer layout
    public static final int SIZE_PER_VERTEX = 5; // x,y,z, s, t
    public static final int STRIDE = 5 * 4;
    public static final int TEX_OFFSET = 3 * 4;
    // each character is drawn as a quad
    public static final int FLOATS_PER_CHAR = SIZE_PER_VERTEX * 4; // 4 vertices per quad
    public static final int ELEMENTS_PER_CHAR = 6;  // 2 triangles per quad
    
    public static int getVertexAttrBufferLen(int numChars) {
        return numChars * FLOATS_PER_CHAR; 
    }
    
    public static int getIndexBufferLen(int numChars) {
        return numChars * ELEMENTS_PER_CHAR;
    }
    
    public BaseFont(IAssetName asset) {
        super(asset);
    }

    public BaseFont(String name) {
        super(name);
    }
 
    public abstract String getTypeFace();
    public abstract boolean isBold();
    public abstract boolean isItalic();
    public abstract int getLineHeight();
    public abstract int getOutline();
    
    /**
     * Fills a buffer with vertex data that represent the given string.
     * Vertex format: x, y, z, s, t
     * @param s
     * @param vertexBuf Buffer to fill
     * @return Number of characters that have fitted into vertexBuf 
     */
    public abstract int fillVertexAttrBuffer(String s, FloatBuffer vertexBuf);
    
    /**
     * Span of text in buffer which was created during last call to
     * fillVertexAttrBuffer(). Span is the distance between the leftmost
     * and rightmost vertex position contained in the buffer.
     * @return Width or 0 in case there was no previous call to fillVertexAttrBuffer()
     */
    public abstract float getVertexBufferSpanX();
    
    /**
     * Span of text in buffer which was created during last call to
     * fillVertexAttrBuffer(). Span is the distance between the upmost
     * and lowermost vertex position contained in the buffer.
     * @return Width or 0 in case there was no previous call to fillVertexAttrBuffer()
     */
    public abstract float getVertexBufferSpanY();
    
    /**
     * x value of leftmost vertex in the buffer which was created during
     * last call to fillVertexAttrBuffer().
     * @return
     */
    public abstract float getVertexBufferLeftX();
    
    /**
     * y value of upmost vertex in the buffer which was created during
     * last call to fillVertexAttrBuffer().
     * @return 
     */
    public abstract float getVertexBufferTopY();
    
    /**
     * Fills a buffer with indices to draw (GL_TRIANGLE_STRIP) vertices built by 
     * fillVertexAttrBuffer(). The buffer is filled completely, so you have to take
     * care to set the number of elements in the draw call to match the vertex
     * buffer.
     * @param elemBuf Buffer to fill
     */
    public abstract void initIndexBuffer(ShortBuffer elemBuf);
    
}
