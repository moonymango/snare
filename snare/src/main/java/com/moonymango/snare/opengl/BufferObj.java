package com.moonymango.snare.opengl;

import static android.opengl.GLES20.*;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Represents a vertex buffer object (GL_ARRAY_BUFFER or
 * GL_ELEMENT_ARRAY_BUFFER). 
 * During VBO creation (glBufferData) BufferObj requests the 
 * actual data from a {@link ISimpleBufferDataProvider} or a 
 * {@link IBufferDataProvider}. When {@link IBufferDataProvider} 
 * is used, then it is also possible to update the VBO
 * (glBufferSubData). 
 */
public class BufferObj extends BaseGLObj {
   
    /**
     * Helper:
     * Allocate native {@link FloatBuffer} and fills with array data.
     * @param buf
     * @return
     */
    public static FloatBuffer convert2FloatBuffer(float[] buf) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(buf.length * Float.SIZE/8); 
        bb.order(ByteOrder.nativeOrder());
        final FloatBuffer fb = bb.asFloatBuffer();
        fb.put(buf, 0, buf.length).position(0);
        return fb;
    }
    
    /**
     * Helper:
     * Allocate native {@link ShortBuffer} and fills with array data.
     * @param buf
     * @return
     */
    public static ShortBuffer convert2ShortBuffer(short[] buf) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(buf.length * Short.SIZE/8);  
        bb.order(ByteOrder.nativeOrder());
        final ShortBuffer sb = bb.asShortBuffer();
        sb.put(buf).position(0);
        return sb;
    }
    
    private IBufferDataProvider mProvider;
    private Target mTarget;
    private int mUsage;
    private final BufferSetup mSetup = new BufferSetup();
    private int mUpdateNum;
    
    protected BufferObj(GLObjDescriptor descriptor) {
        super(descriptor);
        mSetup.clear();
    }

    /**
     * Configures the buffer object. This only marks the buffer,
     * actual buffer creation is done in GL thread, so it has no 
     * overhead to call this multiple times in frame.
     * @param provider
     */
    public void configure(IBufferDataProvider provider) {
        if (isConfigured()) {
            throw new IllegalStateException("Already configured.");
        }
        mProvider = provider;
        setState(GLObjState.TO_LOAD);
    }
    
    /**
     * Enables this buffer contents for specified attribute. 
     * @param p
     */
    public void makeAttribPointer(AttribPointer p)
    {
        if (p.location < 0)
            throw new IllegalArgumentException("Invalid attribute location: " +
            		p.location);
        glBindBuffer(GL_ARRAY_BUFFER, mID);
        glVertexAttribPointer(p.location, p.sizePerVertex, 
                p.type, p.normalize, p.stride, p.offset);
        glEnableVertexAttribArray(p.location);
    }
    
    /**
     * Binds this buffer to GL_ELEMENT_ARRAY target.
     */
    public void makeElementArray()
    {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mID);
    }
    
    /**
     * Update buffer contents. This only marks the buffer,
     * actual update is done in GL thread, so it is possible to change
     * the number of update operations by subsequent calls to update() in
     * a frame. The number given in the last call will be effective for
     * the actual update in GL thread.
     * @param num Number of update operations to perform. Each update operation
     *              is equivalent to a glBufferSubData() call.
     */
    public void update(int num) {
        if (num < 1) {
            throw new IllegalArgumentException("Invalid number of update passes.");
        }
        mUpdateNum = num;
        if (getState() == GLObjState.LOADED) { 
            setState(GLObjState.TO_UPDATE); // onUpdate() will be called
        }
    }
    
    @Override
    public void onLoad() {
        mProvider.getConfigurationSetup(mDescriptor.getQName(), mSetup);
        mTarget = mSetup.mTarget;
        mUsage = mSetup.mUsage;
        if (mUsage != GL_STATIC_DRAW && mUsage != GL_STREAM_DRAW 
                && mUsage != GL_DYNAMIC_DRAW) {
            throw new IllegalArgumentException("Unknown VBO usage.");
        }
        final Buffer buf = mSetup.mBuffer;
        final int size = mSetup.mAutoSize ? getBufferSize(buf) : mSetup.mSize;
        final int pos = mSetup.mAutoSize ? 0 : mSetup.mBufferPos;
        buf.position(pos);
        
        if (mID == INVALID_ID) {
            int[] b = new int[1];
            glGenBuffers(1, b, 0);
            mID = b[0];
        }
        glBindBuffer(mTarget.getIt(), mID);
        glBufferData(mTarget.getIt(), size, buf, mUsage);
        glBindBuffer(mTarget.getIt(), 0);
        
        mSetup.clear();
    }
    
    @Override
    public void onUpdate() { 
        final String name = mDescriptor.getQName();
        for (int pass = 0 ; pass < mUpdateNum; pass++) {
            mProvider.getUpdateSetup(name, pass, mSetup);
            final Buffer buf = mSetup.mBuffer;
            buf.position(mSetup.mBufferPos);
            glBindBuffer(mTarget.getIt(), mID);
            if (mSetup.mSubData) {
                glBufferSubData(mTarget.getIt(), mSetup.mOffset, mSetup.mSize, buf);
            } else {
                glBufferData(mTarget.getIt(), mSetup.mSize, buf, mUsage);
            }
            glBindBuffer(mTarget.getIt(), 0);
            mSetup.clear();
        }
    }

    @Override
    public void onUnload() {
        glBindBuffer(mTarget.getIt(), 0);
        int[] b = {mID};
        glDeleteBuffers(1, b, 0);
        mID = INVALID_ID;
    }

    private static int getBufferSize(Buffer buf) {
        if (buf instanceof FloatBuffer) {
            return buf.capacity() * Float.SIZE/8;
        } else if (buf instanceof ShortBuffer) {
            return buf.capacity() * Short.SIZE/8;
        } 
        throw new IllegalStateException("Invalid data buffer type.");
        
    }
    
    public static enum Target {
        ARRAY,
        ELEMENT;    
        public int getIt() {
            return this == Target.ARRAY ? GL_ARRAY_BUFFER : GL_ELEMENT_ARRAY_BUFFER;
        }   
    }
    
    /**
     * Collects data necessary to create a new VBO.
     */
    public interface IBufferConfigurationSetup {
        /** Buffer containing VBO data. */
        void setBuffer(Buffer buffer);
        /** Sets position of Buffer returned by setBuffer(). Default = 0. */
        void setBufferPosition(int pos);
        /** VBO target. Default = GL_ARRAY_BUFFER. */
        void setTarget(Target target);
        /** Buffer size. Default = 0. */
        void setSize(int size);
        /** 
         * Enable this to load the buffer returned by setBuffer completely 
         * to GPU. setBufferPosition() and setSize() will have no effect when
         * auto size is enabled.
         */
        void enableAutoSize(boolean enabled);
        /** VBO usage. Default = GL_STATIC_DRAW. */
        void setUsage(int usage);
    }
    
    /**
     * Collects data necessary to update a GL buffer object.
     */
    public interface IBufferUpdateSetup {
        /** Buffer containing GL buffer data. */
        void setBuffer(Buffer buffer);
        /** Sets position of Buffer returned by setBuffer(). Default = 0. */
        void setBufferPosition(int pos);
        /** Sets offset into buffer (see glBufferSubData) */
        void setOffset(int offset);
        /** Sets size of data to update (see glBufferSubData) */
        void setSize(int size);
        /** 
         * When enabled then glBufferSubData() is used instead of glBufferData()
         * Default = true 
         */
        void enableBufferSubData(boolean enable);
    }
    
    /**
     * Provider for GL buffer object data.
     */
    public interface IBufferDataProvider {
        /**
         * Collects data necessary to create a GL buffer object.
         * @param name Name of GL buffer object to create.
         * @param setup Setup
         */
        void getConfigurationSetup(String name, IBufferConfigurationSetup setup);
        /**
         * Collects data necessary to update GL buffer object. There may
         * be multiple update passes, depending on what number was given
         * to the update() call of the {@link BufferObj}. So it is possible
         * to modify the buffer contents at several locations. 
         * 
         * @param name Name of buffer object to update.
         * @param pass Index of update pass 
         * @param setup Setup
         */
        void getUpdateSetup(String name, int pass, IBufferUpdateSetup setup);
    }
    
    private static class BufferSetup implements 
            IBufferConfigurationSetup,
            IBufferUpdateSetup {
        
        protected int mOffset;
        protected boolean mAutoSize;
        protected boolean mSubData;
        protected int mSize;
        protected int mBufferPos;
        protected Buffer mBuffer;
        protected Target mTarget;
        protected int mUsage;
        
        @Override
        public void setOffset(int offset) {mOffset = offset;}
        
        @Override
        public void setBuffer(Buffer buffer) {mBuffer = buffer;}
        
        @Override
        public void setBufferPosition(int pos) {mBufferPos = pos;}
        
        @Override
        public void setTarget(Target target) {mTarget = target;}
                
        @Override
        public void enableAutoSize(boolean enable) {mAutoSize = enable;}
        
        @Override
        public void enableBufferSubData(boolean enable) {
            mSubData = enable;
        }

        @Override
        public void setSize(int size) {mSize = size;}
        
        @Override
        public void setUsage(int usage) {mUsage = usage;}
        
        protected void clear() {
            mOffset = 0;
            mAutoSize = false;
            mSubData = true;
            mSize = 0;
            mBuffer = null;
            mBufferPos = 0;
            mTarget = Target.ARRAY;
            mUsage = GL_STATIC_DRAW;
        }
    }
    
    /**
     * Class containing information needed to bind buffer data to an 
     * attribute (glVertexAttribPointer).
     * Just didn't know where else to put this helper class.
     */
    public static class AttribPointer {
        /**
         * Location (index) of attribute.
         */
        public int location = -1;
        public final int sizePerVertex;
        public final int type;
        public final boolean normalize;
        public final int stride;
        public final int offset;
        
        public AttribPointer(int size, int type, boolean normalize, 
                int stride, int offset)
        {
            sizePerVertex = size;
            this.type = type;
            this.normalize = normalize;
            this.stride = stride;
            this.offset = offset;
        }
        
        public AttribPointer(int size, int stride, int offset)
        {
            this(size, GL_FLOAT, false, stride, offset);
        }
        
        /**
         * Setup pointer into specified buffer.
         * @param bufferId
         */
        public void enable(BufferObj b) 
        {
            b.makeAttribPointer(this);
        }
        
        public void disable() 
        {
            glDisableVertexAttribArray(location);
        }
    }
}
