package com.moonymango.snare.res;

public class TextResHandle extends BaseResHandle {
    
    private final String mContent;
    
    public TextResHandle(TextResource res, String content) {   
        super(res);
        mContent = content;
    }
    
    public String getContent() {
        return mContent;
    }
    
}
