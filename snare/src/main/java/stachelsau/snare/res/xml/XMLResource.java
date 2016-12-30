package stachelsau.snare.res.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import stachelsau.snare.game.Game;
import stachelsau.snare.res.BaseResHandle;
import stachelsau.snare.res.BaseResource;
import stachelsau.snare.res.IAssetName;
import stachelsau.snare.util.Logger;
import stachelsau.snare.util.Logger.LogSource;
import android.content.res.AssetManager;
import android.content.res.Resources;

public class XMLResource<T> extends BaseResource {

    private BaseXMLHandler<T> mHandler;
    
    public XMLResource(IAssetName asset, BaseXMLHandler<T> handler) {
        super(asset);
        if (handler == null) {
            throw new IllegalArgumentException("Missing SAX handler.");
        }
        mHandler = handler;
        mHandler.setDescriptor(this);
    }
    
    @Override
    protected BaseResHandle createHandleByAsset(AssetManager am) {
        InputStream in;
        try {
            in = am.open(mName);
            return parse(in);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected BaseResHandle createHandleByResID(Resources res) {
        // never called
        return null;
    }

    @SuppressWarnings("unchecked")
    public XMLResHandle<T> getHandle() {
        return (XMLResHandle<T>) super.getHandle(Game.get().getResourceCache());
    }
    
    private XMLResHandle<T> parse(InputStream in) {
        SAXParserFactory f = SAXParserFactory.newInstance();
        InputSource s = new InputSource(in);
        try {
            SAXParser p = f.newSAXParser();
            XMLReader r = p.getXMLReader();
            r.setContentHandler(mHandler);
            r.parse(s);
        } catch (Exception e) {
            Logger.e(LogSource.RESOURCES, String.format("error parsing %s: %s", mName, e.toString()));
            mHandler.release();
            return null;
        }
        final T content = mHandler.getXMLParseResult();
        mHandler.release();
        return new XMLResHandle<T>(this, content);
    }
    
    /** 
     * Creates and releases handle in one step. Intended for handlers that
     * do not create a new resource but just modify an existing object.
     */
    public void getAndReleaseHandle() {
        releaseHandle(getHandle());
    }

}
