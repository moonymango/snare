package com.moonymango.snare.res.xml;

import java.io.File;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.moonymango.snare.res.xml.BMFont.Char;

/**
 * Handler for parsing BMFont .fnt files.
 */
public class BMFontXMLHandler extends BaseXMLHandler<BMFont> {
    
    private BMFont mFont;

    private String mName;
    private int mSize;
    private boolean mBold;
    private boolean mItalic;
    private int mLineHeight;
    private int mOutline;
    private int mBase;
    private int mWidth;
    private int mHeight;
    
    private String mTextureFileName;
    private String mTextureFileExt;
    private String mTextureFilePath;
    
     
    @Override
    public BMFont getXMLParseResult() {
        return mFont;
    }
    
    @Override
    protected void release() {
        mFont = null;
        mName = null;
        mTextureFileName = null;
        mTextureFileExt = null;
        mTextureFilePath = null;
    }
    
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {}

    @Override
    public void endDocument() throws SAXException {}

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {}

    @Override
    public void startDocument() throws SAXException {
        
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (localName.equals("info")) {
            mName = attributes.getValue("face");
            mSize = parseInt(attributes, "size");
            final int bold = parseInt(attributes, "bold");
            final int italic = parseInt(attributes, "italic");
            mOutline = parseInt(attributes, "outline");
            mBold = bold != 0;
            mItalic = italic != 0;
        }
        
        if (localName.equals("common")) {
            mLineHeight = parseInt(attributes, "lineHeight");
            mBase = parseInt(attributes, "base");
            mWidth = parseInt(attributes, "scaleW");
            mHeight = parseInt(attributes, "scaleH");
            int pages = parseInt(attributes, "pages");
            if (pages != 1) {
                release();
                throw new SAXException("Only one page supported.");
            }
        }
        
        if (localName.equals("page")) {
            mTextureFileName = attributes.getValue("file");
            int i = mTextureFileName.lastIndexOf(".");
            mTextureFileExt = (i > 0) ? mTextureFileName.substring(i) : "";
            if (!mTextureFileExt.equalsIgnoreCase(".png")) {
                release();
                throw new SAXException("Only .png font textures supported.");
            }
            
            // path to the font texture image file is the same as
            // the path to the .fnt file
            File f = new File(mXMLResDescriptor.getName());
            String path = f.getParent();
            
            f = new File(path, mTextureFileName);
            mTextureFilePath = f.getPath(); 
        }
        
        if (localName.equals("chars")) {
            mFont = new BMFont(mName, 
                    mSize, 
                    mBold, 
                    mItalic, 
                    mLineHeight,
                    mBase,
                    mWidth, 
                    mHeight,
                    mOutline,
                    mTextureFilePath);
        }
        
        if (localName.equals("char")) {
            int id = parseInt(attributes, "id");
            if (id < BMFont.MIN_CHAR_ID || id > BMFont.MAX_CHAR_ID) {
                throw new SAXException("Invalid character id: " + id);
            }
            int x = parseInt(attributes, "x");
            int y = parseInt(attributes, "y");
            int width = parseInt(attributes, "width");
            int height = parseInt(attributes, "height");
            int xoffset = parseInt(attributes, "xoffset");
            int yoffset = parseInt(attributes, "yoffset");
            int advance = parseInt(attributes, "xadvance");
            Char chr = new Char((char) id, x, y, width, height, xoffset, yoffset, advance);
            mFont.addChar(chr);
        }
        
    }

}
