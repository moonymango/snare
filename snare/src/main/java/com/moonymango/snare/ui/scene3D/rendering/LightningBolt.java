package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.opengl.BufferObj.AttribPointer;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.util.Geometry;
import com.moonymango.snare.util.VectorAF;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class LightningBolt extends BaseDynamicMeshEffect {

    private static final String VERTEX_SHADER =
            "precision mediump float;                       \n" +
                    
            "uniform mat4  view_proj_matrix;                \n" +
            "uniform mat4  to_world_matrix;                 \n" +
            "uniform vec4  cam_pos;                         \n" +
            "uniform float thickness;                       \n" +
            "uniform float time;                            \n" +
            "uniform float intensity;                       \n" +
            
            "attribute vec4 point;                          \n" +
            "attribute vec4 next_point;                     \n" +
            "attribute vec2 pdata;                          \n" +
            
            "varying vec2  v_tex_coord;                     \n" +
            
            "void main(void) {                              \n" +
                // transform point and next_point to world coords
                "vec4 p = to_world_matrix * point;          \n" +
                "vec4 np = to_world_matrix * next_point;    \n" +
                // directions from point to next point and camera position
                "vec3 dn = np.xyz - p.xyz;                      \n" +
                "vec3 dc = cam_pos.xyz - p.xyz;                 \n" +
                // offset vertex from point along a vector perpendicular to
                // bolt direction and viewing direction
                "float s = pdata.x; \n" + 
                //"float f = s * thickness + intensity *sin(1000.0*pdata.y*time+6.0*pdata.y);  \n" +
                "float f = s * thickness + intensity *sin(floor(pdata.y*time+pdata.y));  \n" +
                "vec3 offs = f * normalize(cross(dc, dn)); \n" +
                //"offs = faceforward(offs, dc, dn); \n" +
                "v_tex_coord = vec2(-fract(time), max(s, 0.0));        \n" +
                "gl_Position = view_proj_matrix * vec4(p.xyz + offs, 1.0);  \n" +
               
            "}";                    
        
    private static final String FRAGMENT_SHADER =
            "precision mediump float;               \n" +
            "uniform sampler2D u_tex;                  \n" +
                
            "varying vec2  v_tex_coord;                   \n" +
            
            "void main(void) {                      \n" + 
                "gl_FragColor = texture2D(u_tex, v_tex_coord);   \n" +
                //"gl_FragColor = vec4(v_tex_coord.t, 0.0, 0.0, 1.0);   \n" +
               
            "}";
    
    private static RenderContext createRenderContext(IGame game) {
        final GLState s = new GLState();
        s.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).enableDepth().lock();
        return new RenderContext(game,
                LightningBolt.class.getName(),
                VERTEX_SHADER,
                FRAGMENT_SHADER,
                s);
    }
    
    private static final int TEX_UNIT = 0;
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource res, TextureObjOptions options)
    {
        final Material m = new Material(res.mGame);
        m.addTextureUnit(new TextureUnit(TEX_UNIT, res, options));
        return m;
    }
    
    private static int muViewProj;
    private static int muToWorld;
    private static int muCamPos;
    private static int muThickness;
    private static int muTime;
    private static int muIntensity;
    private static int muTex;
    
    private static final int STRIDE = 6 * Float.SIZE/8; 
    private static final AttribPointer maPoint = new AttribPointer(4, STRIDE, 0);
    private static final AttribPointer maNextPoint = new AttribPointer(4, STRIDE, 2*STRIDE);
    private static final AttribPointer maPData = new AttribPointer(2, STRIDE, 4 * Float.SIZE/8);
    private static final AttribPointer[] maPointers = {maPoint, maNextPoint, maPData};
        
    private final IBoltPointGenerator mGen;
    private int mSegStart;
    private int mSegEnd;
    private float mSpeed = 1;
    private float mIntensity = 0;
    private float mThickness = 0.1f;
    
    private final int mShrinkF;
    private int mShrinkPos;
    
    /**
     * Constructor. When shrink factor is set to a value >0 then the
     * area of visible segments will be moved towards the origin of the
     * bolt giving the impression that the bolt shrinks or disappears.
     * When the bolt has disappeared completely subsequent calls to 
     * render() have no effect until reset(). Speed of the shrinking 
     * depends on value of shrinkF
     * @param gen
     * @param shrinkF shrinking for values >0, otherwise continuous rendering 
     */
    public LightningBolt(IGame game, IBoltPointGenerator gen, int shrinkF)
    {
        super(createRenderContext(game), new BoltVertexGenerator(game, gen),
                IGame.ClockType.VIRTUAL);
        mGen = gen;   
        mSegEnd = getNumSegments()-1;
        mShrinkF = shrinkF < 1 ? 0 : shrinkF;
    }
    
    /**
     * Constructs lightning bolt for continuous rendering without shrinking.
     * @param gen
     */
    public LightningBolt(IGame game, IBoltPointGenerator gen)
    {
        this(game, gen, 0);
    }

    @Override
    public void extractLocations(String programName, int prog)
    {
        muViewProj = glGetUniformLocation(prog, "view_proj_matrix");
        muToWorld = glGetUniformLocation(prog, "to_world_matrix");
        muCamPos = glGetUniformLocation(prog, "cam_pos");
        muThickness = glGetUniformLocation(prog, "thickness");
        muTime = glGetUniformLocation(prog, "time");
        muIntensity = glGetUniformLocation(prog, "intensity");
        muTex = glGetUniformLocation(prog, "u_tex");
        maPoint.location = glGetAttribLocation(prog, "point");
        maNextPoint.location = glGetAttribLocation(prog, "next_point");
        maPData.location = glGetAttribLocation(prog, "pdata");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass)
    {
        // update 
        mShrinkPos = (int) (mShrinkF * mTime);
        if (mShrinkF > 0 && mShrinkPos > mSegEnd)
            return false;
        
        final float[] viewProjTransform = scene.getCamera().getViewProjectionTransform();
        final float[] toWorld = getGameObj().getToWorld();
        final float[] camPos = scene.getCamera().getPosition();
                         
        bindBuffers(maPointers); 
        
        glUniformMatrix4fv(muViewProj, 1, false, viewProjTransform, 0);
        glUniformMatrix4fv(muToWorld, 1, false, toWorld, 0);
        glUniform4f(muCamPos, camPos[0], camPos[1], camPos[2], camPos[3]);
        glUniform1f(muThickness, mThickness);
        glUniform1f(muTime, mSpeed*mTime);
        glUniform1f(muIntensity, mIntensity);
        glUniform1i(muTex, TEX_UNIT);
       
        final int start = Math.max(mSegStart - mShrinkPos, 0);
        final int end = Math.max(mSegEnd - mShrinkPos, 0);
        final int offs = start * 2 * Short.SIZE/8;  
        final int indices = (end - start + 2) * 2;
        
        glDrawElements(GL_TRIANGLE_STRIP, indices, GL_UNSIGNED_SHORT, offs);

        maPoint.disable();
        maNextPoint.disable();
        maPData.disable();
        return true;
    }
    
    @Override
    public void reset() {
        super.reset();
        mShrinkPos = 0;
    }
    
    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mat == null || !mat.hasTextureUnit(TEX_UNIT))
            throw new IllegalStateException("LightningBolt: invalid material");
    }

    /** Thickness of the bolt. */
    public LightningBolt setThickness(float t) {
        mThickness = t;
        return this;
    }
    
    /** Intensity of bolt bouncing. */
    public LightningBolt setIntensity(float i) {
        mIntensity = i;
        return this;
    }
    
    /** Speed of bolt bouncing. */
    public LightningBolt setSpeed(float s) {
        mSpeed = s;
        return this;
    }
    
    /**
     * Sets the visible segments.
     * @param start First visible segment.
     * @param end Last visible segments.
     * @return
     */
    public LightningBolt setVisibleSegments(int start, int end) 
    {
        if (start < 0 || start > end) 
            throw new IllegalArgumentException("end must not be greater than start.");
            
        mSegStart = Math.min(start, getNumSegments()-1);
        mSegEnd = Math.min(end, getNumSegments()-1); 
        return this;
    }
      
    /** Max. number of visible segments. */
    public int getNumSegments() {
        return mGen.getPointCnt() - 1;
    }
    
    /**
     * Generates base points for {@link LightningBolt}.
     */
    public interface IBoltPointGenerator 
    {
        /** 
         * Gets the number of base points. The lightning bolt will have
         * points-1 segments.
         * @return
         */
        int getPointCnt();
        
        /**
         * Puts point coordinates into the given float array.
         * Note: Value at index 3 of the float array (w component) is ignored
         * during buffer generation.
         * @param point Index of the point (0..getPointCnt())
         * @param coord Array of size 4 to hold coords.
         */
        void getPointData(int point, float[] coord);
        /**
         * Name suffix for GL buffer object to distinct generated buffer 
         * from other generator's buffers.
         */
        String getNameSuffix();
    }
    
    /**
     * Default generator. Generates base points along x axis using the index
     * as x coordinate. Examples:
     * index 0  => coordinates (0, 0, 0)
     * index 5  => coordinates (5, 0, 0)
     */
    public static class LinearBoltPointGenerator implements 
            IBoltPointGenerator
    {
        private final int mSegments;
        
        public LinearBoltPointGenerator(int numSegments) {
            mSegments = numSegments;
        }
        
        public String getNameSuffix() {
            return "default." + mSegments;
        }
        
        @Override
        public int getPointCnt() {
            return mSegments+1;
        }

        @Override
        public void getPointData(int point, float[] coord) {
            coord[0] = point;
            coord[1] = 0;
            coord[2] = 0;
        }
        
    }
    
    public static class CircularBoltPointGenerator implements 
            IBoltPointGenerator
    {
        private final int mSegments;
        private final float[] mVec = {0, 1, 0, 1};
        
        public CircularBoltPointGenerator(int numSegments) {
            mSegments = numSegments;
        }
        @Override
        public int getPointCnt() {
            return mSegments + 1;
        }

        @Override
        public void getPointData(int point, float[] coord) {
            final float angle = 360f / mSegments;
            VectorAF.rotateV(coord, mVec, angle * point, 0, 0, 1);
        }

        @Override
        public String getNameSuffix() {
            return ".circular." + mSegments;
        }
        
    }
    
    /**
     * Generates the vertex and index buffer contents. The location of the
     * bolt's base points is given by an implementation of
     * {@link IBoltPointGenerator}.
     */
    private static class BoltVertexGenerator extends BaseSnareClass implements IVertexGenerator
    {
        private final IBoltPointGenerator mGen;
        
        private BoltVertexGenerator(IGame game, IBoltPointGenerator gen)
        {
            super(game);
            mGen = gen;
        }
        
        @Override
        public String getName() {
            return mGen.getClass().getName() + "." + mGen.getNameSuffix();
        }

        @Override
        public FloatBuffer getVertexAttribs() 
        {
         // segments are defined by base points. one additional basepoint
            // at the end (needed because vertex shader expects
            // actual basepoint and next basepoint as attributes)
            // buffer layout:
            // Each base point creates two vertices on opposite directions
            // but same distance from the base point. There is a buffer entry 
            // for each vertex containing 6 components:
            //  name        description                 attribute
            //  x           base point coord            point, next_point
            //  y           base point coord            point, next_point
            //  z           base point coord            point, next_point
            //  w           base point coord            point, next_point
            //  sign        offset direction            pdata
            //  random      random value                pdata
            //  (random value is same for both vertices of a base point)
            
            // Both vertices are differentiated in vertex shader by the sign 
            // component
            
            // 2 vertices per base point, 6 floats per vertex, one additional
            // base point (need a next_point for the last point)
            final int bp = mGen.getPointCnt() + 1;
            final ByteBuffer vbb = ByteBuffer.allocateDirect(bp * 2 * 
                    6 * Float.SIZE/8); 
            vbb.order(ByteOrder.nativeOrder());
            final FloatBuffer vertexAttribs = vbb.asFloatBuffer();
            final float[] coords = new float[4]; 
            
            for (int i = 0; i < bp; i++)
            {
                mGen.getPointData(i, coords);
                final float rand = mGame.getRandomFloat(Geometry.RAD90,
                        Geometry.RAD360);
                // positive offset
                vertexAttribs.put(coords[0]);
                vertexAttribs.put(coords[1]);
                vertexAttribs.put(coords[2]);
                vertexAttribs.put(1);
                vertexAttribs.put(1);
                vertexAttribs.put(rand);
                // negative offset
                vertexAttribs.put(coords[0]);
                vertexAttribs.put(coords[1]);
                vertexAttribs.put(coords[2]);
                vertexAttribs.put(1);
                vertexAttribs.put(-1);
                vertexAttribs.put(rand);            
            }
            
            vertexAttribs.rewind();
            return vertexAttribs;
        }

        @Override
        public ShortBuffer getIndices() 
        {
            // index buffer: GL_TRIANGLE_STRIP
            // 2 vertices per base point without the additional base point at the
            // end of the buffer
            final int v = 2 * mGen.getPointCnt();
            final ByteBuffer vbb = ByteBuffer.allocateDirect( v * Short.SIZE/8);  
            vbb.order(ByteOrder.nativeOrder());
            final ShortBuffer indices = vbb.asShortBuffer();  
            
            for (int i = 0; i < v; i++) {
                indices.put((short) i);
            }
            indices.rewind();
            return indices;
        }
        
    }
}
