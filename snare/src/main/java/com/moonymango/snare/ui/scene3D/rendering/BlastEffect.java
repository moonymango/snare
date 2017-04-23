package com.moonymango.snare.ui.scene3D.rendering;

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
import com.moonymango.snare.util.IEasingProfile;
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
import static android.opengl.GLES20.glUniformMatrix4fv;

/** 
 * Draws a ring of radius 1 with movable inner edge.
 */
public class BlastEffect extends BaseDynamicMeshEffect {

    private static final String VERTEX_SHADER =
            "precision mediump float;                       \n" +
                    
            "uniform mat4  view_proj_matrix;                \n" +
            "uniform float inner_pos;                       \n" +
            "uniform float outer_pos;                       \n" +
            "uniform float time;                            \n" +
                        
            "attribute vec4 vertex;                          \n" +
            "attribute vec2 tex_coord;                      \n" +
            
            "varying vec2  v_tex_coord;                     \n" +
            
            "void main(void) {                              \n" +
                "v_tex_coord = vec2(time, tex_coord.t);        \n" +
                // set position edge, use tex coord t to differentiate 
                // between inner and outer edge 
                // (t=1 for outer edge and 0 for inner edge)
                "float f = mix(inner_pos, outer_pos, tex_coord.t); \n" +
                "vec4 p = vec4(vertex.xyz * f, 1.0);        \n" +
                "gl_Position = view_proj_matrix * p;  \n" +
               
            "}";                    
        
    private static final String FRAGMENT_SHADER =
            "precision mediump float;               \n" +
            "uniform sampler2D u_tex;                  \n" +
                
            "varying vec2  v_tex_coord;                   \n" +
            
            "void main(void) {                      \n" + 
                "gl_FragColor = texture2D(u_tex, v_tex_coord);   \n" +
                //"gl_FragColor = vec4(v_tex_coord.s, 0.0, 0.0, 1.0);   \n" +
            "}";
    
    private static RenderContext createRenderContext(IGame game) {
        final GLState s = new GLState();
        s.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).enableDepth().lock();
        return new RenderContext(game,
                BlastEffect.class.getName(),
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
    private static int muInnerPos;
    private static int muOuterPos;
    private static int muTime;
    private static int muTex;
    
    private static final int STRIDE = 5 * Float.SIZE/8; 
    private static final AttribPointer maVertex = new AttribPointer(3, STRIDE, 0);
    private static final AttribPointer maTexCoord = new AttribPointer(2, STRIDE, 3*Float.SIZE/8);
    private static final AttribPointer[] maPointers = {maVertex, maTexCoord};
        
    private final int mNumSegments;
    private final IEasingProfile mInnerEdgeProfile;
    private final IEasingProfile mOuterEdgeProfile;
    private final float mSpeed;
    

    /**
     * Constructor.
     * @param segments No. of segments in blast ring
     * @param inner Movement profile for inner edge of blast ring.
     * @param outer Movement profile for outer edge.
     * @param speed Effect speed.
     * @param clock Clock to use.
     */
    public BlastEffect(IGame game, int segments, IEasingProfile inner, IEasingProfile outer, float speed,
                       IGame.ClockType clock)
    {
        super(createRenderContext(game), new BlastVertexGenerator(segments), clock);
        mNumSegments = segments;
        mInnerEdgeProfile = inner;
        mOuterEdgeProfile = outer;
        mSpeed = speed;
    }
    
    @Override
    public void extractLocations(String programName, int prog) {
        muViewProj = glGetUniformLocation(prog, "view_proj_matrix");
        muInnerPos = glGetUniformLocation(prog, "inner_pos");
        muOuterPos = glGetUniformLocation(prog, "outer_pos");
        muTime = glGetUniformLocation(prog, "time");
        muTex = glGetUniformLocation(prog, "u_tex");
        maVertex.location = glGetAttribLocation(prog, "vertex");
        maTexCoord.location = glGetAttribLocation(prog, "tex_coord");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass) {
        final float t = mTime * mSpeed;
        final float frac = (float) (t - Math.floor(t));
        if (t > 1)
            return false;
        
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
                                 
        bindBuffers(maPointers); 
        
        glUniformMatrix4fv(muViewProj, 1, false, viewProjTransform, 0);
        glUniform1f(muInnerPos, mInnerEdgeProfile.value(frac));
        glUniform1f(muOuterPos, mOuterEdgeProfile.value(frac));
        glUniform1f(muTime, t);
        
        glUniform1i(muTex, TEX_UNIT);
        
        final int indices = 2*(mNumSegments+1);
        glDrawElements(GL_TRIANGLE_STRIP, indices, GL_UNSIGNED_SHORT, 0);

        maVertex.disable();
        maTexCoord.disable();
        
        return true;
    }

    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mat == null || !mat.hasTextureUnit(TEX_UNIT))
            throw new IllegalStateException("BlastEffect: invalid material");
    }
    
    private static class BlastVertexGenerator implements IVertexGenerator {

        private final int mNumSegments;
        
        private BlastVertexGenerator(int segments) {
            if (segments < 3) 
                throw new IllegalArgumentException("need at least 3 segments");
            mNumSegments = segments;
        }
        
        @Override
        public String getName() {
            return BlastVertexGenerator.class.getName() + "." + mNumSegments;
        }

        @Override
        public FloatBuffer getVertexAttribs() {
            // Generate ring of vertices with radius 1 at origin in x-z plane 
            // 5 floats per vertex: 
            //      3 x coord, 
            //      2 x texcoord, 
            // 2 vertices per segment: inner + outer edge
            final int len = mNumSegments+1;
            final ByteBuffer vbb = ByteBuffer.allocateDirect(2 * len *
                    6 * Float.SIZE/8);
            vbb.order(ByteOrder.nativeOrder());
            final FloatBuffer vertexAttribs = vbb.asFloatBuffer();
            
            final float angle = 360f / mNumSegments;
            final float texStep = 1f / mNumSegments;
            final float[] vec = {1, 0, 0, 0};
            final float[] coord = new float[4];
            for (int i = 0; i < len; i++) {
                final float a = angle*i;
                final float texS = texStep * i;
                VectorAF.rotateV(coord, vec, a, 0, 1, 0);
                
                // outer edge vertex
                vertexAttribs.put(coord[0]);
                vertexAttribs.put(coord[1]);
                vertexAttribs.put(coord[2]);
                vertexAttribs.put(texS);
                vertexAttribs.put(1);  // tex T
                
                // inner edge vertex
                vertexAttribs.put(coord[0]);
                vertexAttribs.put(coord[1]);
                vertexAttribs.put(coord[2]);
                vertexAttribs.put(texS);
                vertexAttribs.put(0);  // tex T 
            }
            
            vertexAttribs.rewind();
            return vertexAttribs;
        }

        @Override
        public ShortBuffer getIndices() {
            // index buffer: GL_TRIANGLE_STRIP
            // 2 vertices per segment
            final int v = 2 * (mNumSegments+1);
            final ByteBuffer vbb = ByteBuffer.allocateDirect(v * Short.SIZE/8);  
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
