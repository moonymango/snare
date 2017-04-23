package com.moonymango.snare.res.xml;

import com.moonymango.snare.game.IGame;
import com.moonymango.snare.ui.BaseFont;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class BMFont extends BaseFont {
    
    public static final int MAX_CHAR_ID = 255;
    public static final int MIN_CHAR_ID = 0;

    //private final List<Character> mChars = new ArrayList<Character>(MAX_CHAR_ID + 1);
    private final Char[] mChars = new Char[MAX_CHAR_ID + 1];
    
    private final String mTypeFace;
    private final boolean mBold;
    private final boolean mItalic;
    private final int mLineHeight;
    private final int mOutline;
    private final int mWidth;
    private final int mHeight;
    
    private float mSpanX;
    private float mSpanY;
        

    protected BMFont(IGame game, String face, int size, boolean bold, boolean italic,
                     int lineHeight, int base, int width, int height, int outline,
                     String charTexture)
    {
        super(game, charTexture);
        mTypeFace = face;
        mBold = bold;
        mItalic = italic;
        mLineHeight = lineHeight;
        mWidth = width;
        mHeight = height;
        mOutline = outline;
    }
 
    protected void addChar(Char chr) {
        mChars[chr.mChar] = chr;
    }

    /**
     *  setup vertex buffer as collection of quads, each of which drawing
     *  one character of the given string.
     *  TODO prioC: vertex vertical position should not depend on font size
     */
    public int fillVertexAttrBuffer(String s, FloatBuffer vertexBuf) {
        if (vertexBuf == null) {
            throw new IllegalArgumentException("Missing vertex buffer.");
        }
        if (s == null) {
            throw new IllegalArgumentException("Missing string.");
        }
        
        // check how many characters the given buffers can hold
        final int vertexBufChars = vertexBuf.capacity()/FLOATS_PER_CHAR;
        final int len = s.length() > vertexBufChars ? vertexBufChars : s.length();
        int drawn = 0;
                
        mSpanX = 0;
        mSpanY = 0;
        float cursorX = 0;
        float cursorY = 0;
        
        vertexBuf.clear();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c == '\n') {
                // line feed + return
                cursorX = 0;
                cursorY -= mLineHeight;
                continue;
            }
            
            Char chr = mChars[c];
            if (chr == null) {
                continue; // font does not contain this character
            }
            
            //upper left
            vertexBuf.put(cursorX + chr.mXOffset);
            vertexBuf.put(cursorY - chr.mYOffset);
            vertexBuf.put(0);
            vertexBuf.put(chr.mX / mWidth);
            vertexBuf.put(chr.mY / mHeight);
            
            //lower left
            vertexBuf.put(cursorX + chr.mXOffset);
            vertexBuf.put(cursorY - chr.mYOffset - chr.mHeight);
            vertexBuf.put(0);
            vertexBuf.put(chr.mX / mWidth);
            vertexBuf.put((chr.mY + chr.mHeight) / mHeight);
            
            //upper right
            vertexBuf.put(cursorX + chr.mXOffset + chr.mWidth);
            vertexBuf.put(cursorY - chr.mYOffset);
            vertexBuf.put(0);
            vertexBuf.put((chr.mX + chr.mWidth) / mWidth);
            vertexBuf.put(chr.mY / mHeight);
            
            //lower right
            final float x = cursorX + chr.mXOffset + chr.mWidth;
            final float y = cursorY - chr.mYOffset - chr.mHeight; 
            vertexBuf.put(x);
            vertexBuf.put(y);
            vertexBuf.put(0);
            vertexBuf.put((chr.mX + chr.mWidth) / mWidth);
            vertexBuf.put((chr.mY + chr.mHeight) / mHeight);   
            // we only need to check lower right corner to keep track of span,
            // because the vertices grow towards positive x and negative y values
            mSpanX = x > mSpanX ? x : mSpanX;
            mSpanY = y < mSpanY ? y : mSpanY;
            
            cursorX += chr.mAdvance;
            drawn++;
            
        }
        return drawn;
    }
        
    @Override
    public void initIndexBuffer(ShortBuffer elemBuf) {
        final int len = elemBuf.capacity()/ELEMENTS_PER_CHAR;
        for (int i = 0; i < len; i++) {
            int elem = i * 4;
            elemBuf.put((short) elem);        // upper left
            elemBuf.put((short) (elem + 1));  // lower left
            elemBuf.put((short) (elem + 2));  // upper right
            elemBuf.put((short) (elem + 3));  // lower right
            elemBuf.put((short) (elem + 2));  // upper right
            elemBuf.put((short) (elem + 1));  // lower left
        }
    }
    
    @Override
    public float getVertexBufferSpanX() {
        return mSpanX;
    }

    @Override
    public float getVertexBufferSpanY() {
        return Math.abs(mSpanY);
    }
    
    @Override
    public float getVertexBufferLeftX() {
        // vertices are in lower right quadrant,
        // leftmost vertex x is always 0
        return 0;
    }

    @Override
    public float getVertexBufferTopY() {
        // vertices are in lower right quadrant,
        // upmost vertex y is always 0
        return 0;
    }

    @Override
    public boolean isBold() {
        return mBold;
    }

    @Override
    public boolean isItalic() {
        return mItalic;
    }

    @Override
    public int getLineHeight() {
        return mLineHeight;
    }

    @Override
    public int getOutline() {
        return mOutline;
    }

    @Override
    public String getTypeFace() {
        return mTypeFace;
    }
    
    public static class Char {
        public Char(char chr, int x, int y, int width, int height,
                int xOffset, int yOffset, int advance) {
            mChar = chr;
            mX = x;
            mY = y;
            mWidth = width;
            mHeight = height;
            mXOffset = xOffset;
            mYOffset = yOffset;
            mAdvance = advance;
        }
        
        public final char mChar;
        public final float mX;
        public final float mY;
        public final float mWidth;
        public final float mHeight;
        public final float mXOffset;
        public final float mYOffset;
        public final float mAdvance;
    }
    
}
