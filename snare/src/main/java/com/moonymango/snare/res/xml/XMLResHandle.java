package com.moonymango.snare.res.xml;

import com.moonymango.snare.res.BaseResHandle;

public class XMLResHandle<T> extends BaseResHandle {

    private final T mContent;
    
    public XMLResHandle(XMLResource<T> res, T content) {
        super(res);
        mContent = content;
    }
    
    public T getContent() {
        return mContent;
    }

}
