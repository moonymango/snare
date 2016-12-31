package com.moonymango.snare.res.xml;

import java.io.File;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.res.texture.BaseTextureResource.ITextureRegionProvider;
import com.moonymango.snare.res.texture.BaseTextureResource.TextureRegion;
import com.moonymango.snare.res.texture.BitmapTextureResource;
import com.moonymango.snare.res.texture.ETC1TextureResource;

/**
 * Handler for Crazy Eddies GUI system image set files. 
 */
public class CEGUIImageSetXMLHandler extends BaseXMLHandler<BaseTextureResource> {

    private BaseTextureResource mTexture;
    private String mName;
    private String mFileName;
    private String mFilePath;
    private int mNativeHorzRes;
    private int mNativeVertRes;
    private CEGUITextureRegionProvider mProvider;
    private ImageFileType mType;

    
    @Override
    protected BaseTextureResource getXMLParseResult() {
        return mTexture;
    }
    
    @Override
    protected void release() {
        mTexture = null;
        mName = null;
        mFileName = null;
        mFilePath = null;
        mProvider = null;
        mType = null;
    }

    @Override
    public void endDocument() throws SAXException {
        switch (mType) {
        case PNG:
            mTexture = new BitmapTextureResource(mFilePath, mProvider);
            return;
            
        case PKM:
            mTexture = new ETC1TextureResource(mFilePath, mProvider);
            return;
            
        default:
            throw new SAXException("Unknown image file type.");
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (localName.equals("Imageset")) {
            handleImageset(attributes);
        }
        if (localName.equals("Image")) {
            handleImage(attributes);
        }
    }

    private void handleImageset(Attributes attributes) throws SAXException {
        if (mProvider != null) {
            return; // we already have a region provider built, so ignore tag 
        }
        mName = attributes.getValue("Name");
        if (!mName.equals(mXMLResDescriptor.getQualifier())) {
            mName = null;
            return;
        }
        
        // check image file type
        mFileName = attributes.getValue("Imagefile");
        int i = mFileName.lastIndexOf('.');
        final String ext = (i > 0) ? mFileName.substring(i) : "";
        if (ext.equalsIgnoreCase(".png")) {
            mType = ImageFileType.PNG;
        }
        if (ext.equalsIgnoreCase(".pkm")) {
            mType = ImageFileType.PKM;
        }
        if (mType == null) {
            throw new SAXException("Only .png and .pkm textures supported.");
        }
        
        // image file is expected in same directory as the xml file
        File f = new File(mXMLResDescriptor.getName());
        String path = f.getParent();   
        f = new File(path, mFileName);
        mFilePath = f.getPath(); 
    
        // image dimensions
        mNativeHorzRes = parseInt(attributes, "NativeHorzRes");
        mNativeVertRes = parseInt(attributes, "NativeVertRes");
        if (mNativeHorzRes <= 0 || mNativeVertRes <= 0) {
            throw new SAXException("Invalid texture size.");
        }
        
        mProvider = new CEGUITextureRegionProvider();
    }
    
    private void handleImage(Attributes attributes) throws SAXException {
        if (mProvider == null) {
            return;
        }
        
        final String name = attributes.getValue("Name");
        final int x = parseInt(attributes, "XPos");
        final int y = parseInt(attributes, "YPos");
        final int width = parseInt(attributes, "Width");
        final int height = parseInt(attributes, "Height");
        if (x < 0 || y < 0 || width <= 0 || height <= 0) {
            throw new SAXException("Invalid region data.");
        }
        final float left = (float) x / mNativeHorzRes;
        final float right = (float) (x + width) / mNativeHorzRes;
        final float top = (float) (y + height) / mNativeVertRes;
        final float bottom = (float) y / mNativeVertRes;
        if (left >= right || bottom >= top) {
            throw new SAXException("Invalid region data.");
        }
        
        final TextureRegion r = new TextureRegion(true, left, right, top, bottom);
        mProvider.mRegions.put(name, r);
        
    }
    
    private static class CEGUITextureRegionProvider implements ITextureRegionProvider {

        private final HashMap<String, TextureRegion> mRegions = new HashMap<String, 
                BaseTextureResource.TextureRegion>();
        
        public TextureRegion getRegionByName(String name) {
            return mRegions.get(name);
        }
        
    }
    
    private enum ImageFileType {
        PNG,
        PKM
    }
}
