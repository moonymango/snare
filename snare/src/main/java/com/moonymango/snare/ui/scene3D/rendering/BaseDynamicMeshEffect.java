package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.opengl.BufferObj;
import com.moonymango.snare.opengl.BufferObj.AttribPointer;
import com.moonymango.snare.opengl.BufferObj.IBufferConfigurationSetup;
import com.moonymango.snare.opengl.BufferObj.IBufferDataProvider;
import com.moonymango.snare.opengl.BufferObj.IBufferUpdateSetup;
import com.moonymango.snare.opengl.BufferObj.Target;
import com.moonymango.snare.opengl.GLObjDescriptor;
import com.moonymango.snare.opengl.GLObjDescriptor.GLObjType;
import com.moonymango.snare.ui.scene3D.BaseEffect;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Base class for effects that bring their own mesh, i.e. particle effects. 
 * Generates VBOs for vertices and indices. Override getVertexAttribs() 
 * and getIndices() to set the VBO content. Default implementation generates 
 * a vertex buffer where each vec4 element describes a corner of a particle quad. 
 * E.g. element (-1, -1, z, w) is a lower left corner, (1, 1, z, w) is an upper 
 * right corner. z and w are random numbers within [0..1] that allow to 
 * differentiate between quads in vertex shader, i.e. for all 4 corners of a 
 * quad z and w have the same value, but they are different across quads. 
 * Default index buffer is intended to be rendered using GL_TRIANGLE_STRIP. 
 * 
 * This class also provides methods to set up texture objects.
 */
public abstract class BaseDynamicMeshEffect extends BaseEffect 
        implements IBufferDataProvider {
    
    /** Time in seconds. */
    protected float mTime;
    private final IGame.ClockType mClock;
    
    private final IVertexGenerator mGen;
    
    private final GLObjDescriptor mVertexBufferDescr;
    private BufferObj mVertexBufferObj;
    private final GLObjDescriptor mIndexBufferDescr;
    private BufferObj mIndexBufferObj;
    
    //private TextureUnit[] mTextures;
    
    /**
     * Constructor takes data to compile program and generate particle buffer.
     * Use sParticlePointer for particle buffer.
     *
     * @param clock Clock type for time updates.
     */
    protected BaseDynamicMeshEffect(RenderContext context, IVertexGenerator gen, IGame.ClockType clock)
    {
        super(context);
        
        mClock = clock;
        final String n = gen.getName();
        mVertexBufferDescr = new GLObjDescriptor(context.mGame, n + ".vertices", GLObjType.BUFFER);
        mIndexBufferDescr = new GLObjDescriptor(context.mGame, n + ".indices", GLObjType.BUFFER);
        mGen = gen;
    }
 
    @Override
    public void loadToGpu() {
        super.loadToGpu();
        
        mVertexBufferObj = (BufferObj) mVertexBufferDescr.getHandle();
        if (!mVertexBufferObj.isConfigured()) {
            mVertexBufferObj.configure(this);
        }
        
        mIndexBufferObj = (BufferObj) mIndexBufferDescr.getHandle();
        if (!mIndexBufferObj.isConfigured()) {
            mIndexBufferObj.configure(this);
        }
           
    }

    @Override
    public void unloadFromGpu() {
        super.unloadFromGpu();
        mVertexBufferDescr.releaseHandle(mVertexBufferObj);
        mVertexBufferObj = null;
        mIndexBufferDescr.releaseHandle(mIndexBufferObj);
        mIndexBufferObj = null;
    }

    public void onUpdate(long realTime, float realDelta, 
            float virtualDelta) {
        // update time so that particles will move
        final float delta = mClock == IGame.ClockType.REALTIME ? realDelta
                : virtualDelta;
        mTime += delta / 1000;
    }
     
    @Override
    public void getConfigurationSetup(String name,
            IBufferConfigurationSetup setup) {
        if (name.equals(mVertexBufferDescr.getQName())) {
            setup.setBuffer(mGen.getVertexAttribs());
            setup.enableAutoSize(true);
            setup.setTarget(Target.ARRAY);
            return;
        }
        if (name.equals(mIndexBufferDescr.getQName())) {
            setup.setBuffer(mGen.getIndices());
            setup.enableAutoSize(true);
            setup.setTarget(Target.ELEMENT);
        }
        
    }

    @Override
    public void getUpdateSetup(String name, int pass, IBufferUpdateSetup setup) {
        throw new UnsupportedOperationException("Buffer update not supported.");
    }

    /** 
     * Sets up vertex array pointers into VBO and also binds textures
     * to texture units.
     */
    public void bindBuffers(AttribPointer[] p) 
    {    
        for (int i = 0; i < p.length; i++)
            p[i].enable(mVertexBufferObj);
        
        mIndexBufferObj.makeElementArray();
    }

    /** 
     * @return Vertex generator used by this effect.
     */
    protected IVertexGenerator getGenerator() {return mGen;}
    
    public void reset() {
        mTime = 0;
    }

    public interface IVertexGenerator 
    {
        /** 
         * Name of the generator. Will be used to derive names for vertex 
         * and index {@link BufferObj}s
         */
        String getName();
        /** Returns vertex buffer contents. */
        FloatBuffer getVertexAttribs(); 
        /** Returns the index buffer contents. */
        ShortBuffer getIndices();
    }
    
    
    public static class DefaultParticleGenerator extends BaseSnareClass implements IVertexGenerator
    {
        private final int mParticleCnt;

        public DefaultParticleGenerator(IGame game, int particleCnt)
        {
            super(game);
            if (particleCnt < 1) 
                throw new IllegalArgumentException("Need at least one particle.");
            mParticleCnt = particleCnt;
        }
        
        @Override
        public String getName() {
            return DefaultParticleGenerator.class.getName() + "." +
                    mParticleCnt;
        }

        @Override
        public FloatBuffer getVertexAttribs() 
        {    
            // 4 vertices per particle quad, 4 floats per vertex
            final ByteBuffer vbb = ByteBuffer.allocateDirect(mParticleCnt * 4 * 4 * Float.SIZE/8); 
            vbb.order(ByteOrder.nativeOrder());
            final FloatBuffer vertexAttribs = vbb.asFloatBuffer();
            
            for (int i = 0; i < mParticleCnt; i++) {
                // get z,w for the quads
                final float z = mGame.getRandomFloat(0, 1);
                final float w = mGame.getRandomFloat(0, 1);
                
                // upper left
                vertexAttribs.put(-1);
                vertexAttribs.put(1);
                vertexAttribs.put(z);
                vertexAttribs.put(w);
                // lower left
                vertexAttribs.put(-1);
                vertexAttribs.put(-1);
                vertexAttribs.put(z);
                vertexAttribs.put(w);
                // upper right
                vertexAttribs.put(1);
                vertexAttribs.put(1);
                vertexAttribs.put(z);
                vertexAttribs.put(w);
                // lower right
                vertexAttribs.put(1);
                vertexAttribs.put(-1);
                vertexAttribs.put(z);
                vertexAttribs.put(w);
            }
            vertexAttribs.rewind();
            return vertexAttribs;
        }

        @Override
        public ShortBuffer getIndices() 
        {
            // index buffer: 6 indices per quad:  4 vertices (GL_TRIANGLE_STRIP) + 2 indices 
            // to produce degenerate triangles
            final ByteBuffer vbb = ByteBuffer.allocateDirect(mParticleCnt * 6 * Short.SIZE/8);  
            vbb.order(ByteOrder.nativeOrder());
            final ShortBuffer indices = vbb.asShortBuffer();  
            
            int j = 0;
            for (int i = 0; i < mParticleCnt; i++) {
                indices.put((short) j);
                indices.put((short) (j + 1));
                indices.put((short) (j + 2));
                indices.put((short) (j + 3));
                
                // degenerate triangles to separate quads
                indices.put((short) (j + 3));
                j = (i + 1) * 4;
                indices.put((short) j);
            }
            indices.rewind();
            return indices;
        }
        
        /**
         * @return Number of indices in particle index buffer.
         */
        public int getParticleIndexCount() 
        {
            // index buffer: 6 indices per quad:  4 vertices (GL_TRIANGLE_STRIP) + 
            // 2 indices to produce degenerate triangles
            return mParticleCnt*6;
        }
        
        /**
         * @return Pointer matching the default particle buffer layout.
         */
        public AttribPointer getParticlePointer()
        {
            return new AttribPointer(4, 4 * Float.SIZE/8, 0);
        }
    }
}