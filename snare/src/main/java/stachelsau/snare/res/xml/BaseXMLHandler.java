package stachelsau.snare.res.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public abstract class BaseXMLHandler<T> extends DefaultHandler {
    
    protected XMLResource<?> mXMLResDescriptor;
    
    protected static int parseInt(Attributes attr, String name) {
        return Integer.parseInt(attr.getValue(name));
    }
    
    protected static boolean parseBoolean(Attributes attr, String name) {
        return Boolean.parseBoolean(attr.getValue(name));
    }
    
    protected static float parseFloat(Attributes attr, String name) {
        return Float.parseFloat(attr.getValue(name));
    }
    
    protected static int parseInt(Attributes attr, String name,
            int defaultResult) {
        final String s = attr.getValue(name);
        return s == null ? defaultResult : Integer.parseInt(s);
    }
    
    protected static boolean parseBoolean(Attributes attr, String name,
            boolean defaultResult) {
        final String s = attr.getValue(name);
        return s == null ? defaultResult : Boolean.parseBoolean(s);            
    }
    
    protected static float parseFloat(Attributes attr, String name,
            float defaultResult) {
        final String s = attr.getValue(name);
        return s == null ? defaultResult : Float.parseFloat(s);            
    }
    
    protected static String parseString(Attributes attr, String name, 
            String defaultResult) {
        final String s = attr.getValue(name);
        return s == null ? defaultResult : s;
    }

    /** Override to return the result */
    protected abstract T getXMLParseResult();
    
    /** 
     * Override to release everything created during parsing.
     * This function should reset the handler to a state that
     * enables a new parse process.
     */
    protected abstract void release();
    
    protected final void setDescriptor(XMLResource<?> descr) {
        mXMLResDescriptor = descr;
    }
  
}
