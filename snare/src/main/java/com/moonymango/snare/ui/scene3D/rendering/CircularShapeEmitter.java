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

import static android.opengl.GLES20.GL_ONE;
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

//TODO prioC: add texture handling like in PyramidShapeEmitter
public class CircularShapeEmitter extends BaseDynamicMeshEffect {
    
    private static final String VERTEX_SHADER =
            "precision mediump float;               \n" +
    
            "uniform float time_0_X;                \n" +
            "uniform vec4 cameraX;                  \n" +
            "uniform vec4 cameraY;                  \n" +
            "uniform mat4  view_proj_matrix;        \n" +
            
            "uniform float particleSize;            \n" +
            "uniform float radius;                  \n" +
            "uniform float delta_radius;            \n" +
            "uniform float delta_y;                 \n" +
            
            "attribute vec4 rm_Vertex;              \n" +
            
            "varying vec2  vTexCoord;               \n" +
            "varying float vColor;                  \n" +
            
            "const float pi2 = 6.29;                \n" +
            "const float ONE = 1.0;                 \n" +
            
            "void main(void) {                      \n" +
               "float t = fract(time_0_X);        \n" +
               "float r = t * radius * (1.0 - delta_radius * rm_Vertex.w);        \n" +
            
               "vec3 pos;                                                   \n" +
               // Spread particles
               "pos.x = r * cos(pi2 * rm_Vertex.z);         \n" +
               "pos.z = r * sin(pi2 * rm_Vertex.z);         \n" +
               "pos.y = (delta_y * cos(163.0 * rm_Vertex.w)) * t;                    \n" +
                             
               // Billboard the quads.
               "pos += particleSize * (rm_Vertex.x * cameraX + rm_Vertex.y * cameraY).xyz;  \n" +
              
               "gl_Position = view_proj_matrix * vec4(pos, ONE);                \n" +
               "vTexCoord = (rm_Vertex.xy + ONE) / 2.0;                         \n" +
               "vColor    = ONE - t;                                     \n" +
            "}";                    
        
    private static final String FRAGMENT_SHADER =
            "precision mediump float;               \n" +
            "uniform sampler2D particle;               \n" +
        
            "varying vec2  vTexCoord;               \n" +
            "varying float vColor;                  \n" +
        
            "void main(void) {                      \n" +
                "vec4 tex = texture2D(particle, vTexCoord);            \n" +
                "gl_FragColor = vec4(tex.rgb, vColor * tex.r); \n" + 
               
            "}";
                   
    private static RenderContext createRenderContext(IGame game)
    {
        final GLState s = new GLState();
        s.enableDepth().enableDepthMask(false).enableBlend(GL_SRC_ALPHA, GL_ONE)
                .lock();
        return new RenderContext(game,
                CircularShapeEmitter.class.getName(),
                VERTEX_SHADER, FRAGMENT_SHADER, s);
    }
    
    private static final int TEX_UNIT = 0;
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource res, TextureObjOptions options)
    {
        final Material m = new Material(res.mGame);
        m.addTextureUnit(new TextureUnit(TEX_UNIT, res, options));
        return m;
    }
   
    private static final String U_TIME      = "time_0_X";
    private static final String U_CAMERA_X  = "cameraX";
    private static final String U_CAMERA_Y  = "cameraY";
    private static final String U_VIEWPROJ_TRANSFORM = "view_proj_matrix";
    private static final String U_PARTICLE_SIZE    = "particleSize";
    private static final String U_RADIUS = "radius";
    private static final String U_DELTA_RADIUS  = "delta_radius";
    private static final String U_DELTA_Y       = "delta_y";
    private static final String U_PARTICLE     = "particle";
    private static final String A_VERTEX       = "rm_Vertex";
    
    private static int muTime;
    private static int muCameraX;
    private static int muCameraY;
    private static int muViewProj;
    private static int muParticleSize;
    private static int muRadius;
    private static int muDeltaRadius;
    private static int muDeltaY;
    private static int muParticle;
    private static final AttribPointer[] maPointers = new AttribPointer[1];
    
    
    private float mSize = 0.1f;
    private float mSpeed = 1f;
    private float mRadius = 1f;
    private float mDeltaRadius = 0.2f;
    private float mDeltaY = 0.2f;
    
    private final DefaultParticleGenerator mGen;
    private final boolean mRunOnce;
    
    

    public CircularShapeEmitter(IGame game, int paricleCnt, boolean runOnce)
    {
        this(game, paricleCnt, runOnce, IGame.ClockType.VIRTUAL);
    }
    
    public CircularShapeEmitter(IGame game, int paricleCnt, boolean runOnce, IGame.ClockType clock)
    {
        super(createRenderContext(game), new DefaultParticleGenerator(game, paricleCnt), clock);
        mRunOnce = runOnce;
        
        // use default buffer and pointer
        mGen = (DefaultParticleGenerator) getGenerator();
        if (maPointers[0] == null)
            maPointers[0] = mGen.getParticlePointer();
    }

    public void extractLocations(String programName, int prog) {
        muTime = glGetUniformLocation(prog, U_TIME);
        muCameraX = glGetUniformLocation(prog, U_CAMERA_X);
        muCameraY = glGetUniformLocation(prog, U_CAMERA_Y);
        muViewProj = glGetUniformLocation(prog, U_VIEWPROJ_TRANSFORM);
        muParticleSize = glGetUniformLocation(prog, U_PARTICLE_SIZE);
        muRadius = glGetUniformLocation(prog, U_RADIUS);
        muDeltaRadius = glGetUniformLocation(prog, U_DELTA_RADIUS);
        muDeltaY = glGetUniformLocation(prog, U_DELTA_Y);
        muParticle = glGetUniformLocation(prog, U_PARTICLE);
        maPointers[0].location = glGetAttribLocation(prog, A_VERTEX);
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass)
    {    
        // note: ignore delivered mesh!!   
        final float progress = mTime * mSpeed;
        if (progress >= 1.0f && mRunOnce) {
            return false;
        }
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] viewTransformInv = scene.getViewTransformStack().getInv();
        
        //material.bindTextures();
        bindBuffers(maPointers); 
        
        glUniformMatrix4fv(muViewProj, 1, false, viewProjTransform, 0);
        glUniform4f(muCameraX, viewTransformInv[0], viewTransformInv[1], 
                viewTransformInv[2], viewTransformInv[3]);
        glUniform4f(muCameraY, viewTransformInv[4], viewTransformInv[5], 
                viewTransformInv[6], viewTransformInv[7]);
        glUniform1f(muTime, progress);
        glUniform1f(muParticleSize, mSize);
        glUniform1f(muRadius, mRadius);
        glUniform1f(muDeltaRadius, mDeltaRadius);
        glUniform1f(muDeltaY, mDeltaY);
        glUniform1i(muParticle, TEX_UNIT);
               
        glDrawElements(GL_TRIANGLE_STRIP, mGen.getParticleIndexCount(), 
                GL_UNSIGNED_SHORT, 0);
        
        maPointers[0].disable();
        return true;
    }
    
    public void setParticleSize(float size) {
        mSize = size;
    }
    
    public void setSpeed(float speed) {
        mSpeed = speed;
    }
    
    public void setRadius(float radius) {
        mRadius = radius;
    }
    
    public void setSpread(float spread) {
        mDeltaRadius = spread;
        mDeltaY = spread;
    }

    @Override
    protected void check(BaseMesh mesh, Material mat) 
    {
        if (mat == null || !mat.hasTextureUnit(TEX_UNIT))
            throw new IllegalStateException("CirularShapeEmitter: " +
            		"invalid material");
    }
    
}
