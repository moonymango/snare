package stachelsau.snare.opengl;

import static android.opengl.GLES20.*;
import stachelsau.snare.game.Game;
import stachelsau.snare.opengl.GLObjDescriptor.GLObjType;
import stachelsau.snare.opengl.TextureObj.TextureSize;

public class FramebufferObj extends BaseGLObj {

    private TextureSize mSize;
    private TextureObjOptions mOptions;
    private GLObjDescriptor mColorAttachmentDescr;
    private GLObjDescriptor mDepthAttachmentDescr;
    private RenderbufferObj mDepthAttachmentR;
    private TextureObj mColorAttachment;
    
    protected FramebufferObj(GLObjDescriptor descriptor) {
        super(descriptor);
    }
    
    /**
     * Configures FBO with 2D texture as color attachment and renderbuffer 
     * object as depth attachment, no stencil attachment. 
     * @param size
     * @param colorOptions Texture options for color attachment texture
     */
    public void configure(TextureSize size, TextureObjOptions colorOptions) {
        if (isConfigured()) {
            throw new IllegalStateException("FBO already configured.");
        }
        
        final GLInfo gl = Game.get().getRenderer().getInfo();
        final int maxSize = Math.min(gl.getMaxRenderbufferSize(), 
                gl.getMaxTextureSize());
        if (size.value() > maxSize) {
            throw new IllegalStateException("Unsupported framebuffer size.");
        }
        mSize = size;
        mOptions = colorOptions != null ? colorOptions : 
            Game.get().getSettings().mDefaultTextureOptions;
        
        // color attachment
        mColorAttachmentDescr = new GLObjDescriptor(mDescriptor.getQName()
                + Game.DELIMITER + "color", GLObjType.TEXTURE);
        mColorAttachment = (TextureObj) mColorAttachmentDescr.getHandle();
        mColorAttachment.configure(mSize, mOptions);
        
        // depth attachment
        mDepthAttachmentDescr = new GLObjDescriptor(mDescriptor.getQName()
                + Game.DELIMITER + "depth", GLObjType.RENDERBUFFER);
        mDepthAttachmentR = (RenderbufferObj) mDepthAttachmentDescr.getHandle();
        mDepthAttachmentR.configureDepth(mSize.value(), mSize.value());
        
        setState(GLObjState.TO_LOAD);
    }
    
    /**
     * @return ID of color attachment texture.
     */
    public int getColorAttachmentId() {
        if (!isLoaded()) {
            throw new IllegalStateException("Framebuffer not ready.");
        }
        return mColorAttachment.getID();
    }
    
    @Override
    public void onLoad() {
        // Relies on the "LIFO" order of the GLObjCache's update() 
        // method: because the attachments handles were created AFTER
        // the handle of the framebuffer itself (see configure()), they were
        // loaded to gpu BEFORE the framebuffer. Therefore the attachments
        // are already in the GPU when this gets called 
        
        if (mID == INVALID_ID) {
            int[] b = new int[1];
            glGenFramebuffers(1, b, 0);
            mID = b[0];
        }  
       
        glBindFramebuffer(GL_FRAMEBUFFER, mID);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, 
                GL_TEXTURE_2D, mColorAttachment.getID(), 0);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, 
                GL_RENDERBUFFER, mDepthAttachmentR.getID());
        int stat = glCheckFramebufferStatus(GL_FRAMEBUFFER); 
        if ( stat != GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Incomplete framebuffer: " + stat);
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onUnload() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int[] t = {mID};
        glDeleteFramebuffers(1, t, 0);
        mID = INVALID_ID;
        
        mColorAttachmentDescr.releaseHandle(mColorAttachment);
        mDepthAttachmentDescr.releaseHandle(mDepthAttachmentR);
        mColorAttachment = null;
        mDepthAttachmentR = null;
    }
  
}
