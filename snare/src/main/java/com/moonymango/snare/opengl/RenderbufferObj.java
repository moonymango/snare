package com.moonymango.snare.opengl;

import static android.opengl.GLES20.*;

import com.moonymango.snare.game.SnareGame;

public class RenderbufferObj extends BaseGLObj {

    private int mWidth;
    private int mHeight;
    private int mFormat;
    
    protected RenderbufferObj(GLObjDescriptor descriptor) {
        super(descriptor);
    }
    
    /**
     * Configure as depth renderbuffer object.
     * @param width
     * @param height
     */
    public void configureDepth(int width, int height) {
        configure(GL_DEPTH_COMPONENT16, width, height);
    }
    
    /**
     * Configure as color renderbuffer (RGB565).
     * @param width
     * @param height
     */
    public void configureColor(int width, int height) {
        configure(GL_RGB565, width, height);
    }
    
    private void configure(int format, int width, int height) {
        if (isConfigured()) {
            throw new IllegalStateException("Already configured.");
        }
        mFormat = format;
        final int maxSize = SnareGame.get().getRenderer().getInfo()
                .getMaxRenderbufferSize();
        if (width < 1 || width > maxSize 
                || height < 1 || height > maxSize) {
            throw new IllegalStateException("Invalid renderbuffer size.");
        }
        mWidth = width;
        mHeight = height;
        
        setState(GLObjState.TO_LOAD);
    }
    
    @Override
    public void onLoad() {
        if (mID == INVALID_ID) {
            int[] b = new int[1];
            glGenRenderbuffers(1, b, 0);
            mID = b[0];
        }  
        glBindRenderbuffer(GL_RENDERBUFFER, mID);
        glRenderbufferStorage(GL_RENDERBUFFER, mFormat, mWidth, mHeight);
        checkGLError();
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
    }

    @Override
    public void onUnload() {
        glBindRenderbuffer(GL_RENDERBUFFER, 0);
        int[] rb = {mID};
        glDeleteRenderbuffers(1, rb, 0);
        mID = INVALID_ID;
    }

}
