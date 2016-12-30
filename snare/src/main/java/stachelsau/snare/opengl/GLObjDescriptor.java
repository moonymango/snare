package stachelsau.snare.opengl;

import stachelsau.snare.game.Game;
import stachelsau.snare.util.CacheItemDescriptor;
import stachelsau.snare.util.Logger;
import stachelsau.snare.util.Logger.LogSource;

public class GLObjDescriptor extends CacheItemDescriptor<GLObjCache, GLObjDescriptor, BaseGLObj> {
    
    private final GLObjType mType;

    public GLObjDescriptor(String name, GLObjType type) {
        super(name, null); 
        if (type == null) {
            throw new IllegalArgumentException("Missing type.");
        }
        mType = type;
    }

    public BaseGLObj getHandle() {
        return getHandle(Game.get().getGLObjCache());
    }
   
    @Override
    protected BaseGLObj createCacheItem() {
        Logger.i(LogSource.OPENGL, "item created: " + mQName);
        switch (mType) {
        case BUFFER:
            return new BufferObj(this);
        case TEXTURE:
            return new TextureObj(this);
        case PROGRAM:
            return new ProgramObj(this);
        case RENDERBUFFER:
            return new RenderbufferObj(this);
        case FRAMEBUFFER:
            return new FramebufferObj(this);
        }
        return null;
    }

    public enum GLObjType {
        BUFFER,
        TEXTURE,
        PROGRAM,
        RENDERBUFFER,
        FRAMEBUFFER;
    }
}
