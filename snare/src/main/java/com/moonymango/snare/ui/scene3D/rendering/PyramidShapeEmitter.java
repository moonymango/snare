package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.opengl.BufferObj.AttribPointer;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.res.texture.BitmapTextureResource.ITextureChannelSource.Channel;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;

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

/**
 * Based on AMD's RenderMonkey Particle System example.
 * </br>
 * This effect uses it's textures like this:
 * </br>
 * Particle color and alpha is fetched from color texture based on 
 * particle lifetime t and an adjustable variable y (see setColorY()): 
 * </br>
 * texCoords = vec2(t, y)
 * </br>
 * That means, that color is taken from a line between coordinates 
 * (1,0) and (0,0) from the texture.
 * </br>
 * A second alpha value is fetched from the shape texture with the fragment's
 * regular texture coordinates. The final alpha value is the product of
 * the color alpha and the shape alpha value. 
 */
public class PyramidShapeEmitter extends BaseDynamicMeshEffect {
    
    private static final String VERTEX_SHADER =
            "precision mediump float;               \n" +
    
            "uniform float time_0_X;                \n" +
            "uniform vec4 cameraX;                  \n" +
            "uniform vec4 cameraY;                  \n" +
            "uniform mat4  view_proj_matrix;        \n" +
            "uniform float particleSystemHeight;    \n" +
            "uniform float particleSpeed;           \n" +
            "uniform float particleSpread;          \n" +
            "uniform float particleSystemShape;     \n" +
            "uniform float particleShape;           \n" +
            "uniform float particleSize;            \n" +
            
            "attribute vec4 rm_Vertex;              \n" +
            
            "varying vec2  vTexCoord;               \n" +
            "varying float vS;                  \n" +
            
            "const float ONE = 1.0;                \n" +
            
            "void main(void) {                      \n" +
               // Loop particles
               "float t = fract(rm_Vertex.w + particleSpeed * time_0_X);    \n" +
               // Determine the shape of the system
               "float s = pow(t, particleSystemShape);                      \n" +
            
               "vec3 pos;                                                   \n" +
               // Spread particles in a semi-random fashion
               "pos.x = particleSpread * s * cos(62.0 * rm_Vertex.z);       \n" +
               "pos.z = particleSpread * s * cos(163.0 * rm_Vertex.z);      \n" +
               // Particles goes up
               "pos.y = particleSystemHeight * t;                           \n" +
                             
               // Billboard the quads.
               "pos += particleSize * (rm_Vertex.x * cameraX + rm_Vertex.y * cameraY).xyz;  \n" +
              
               "gl_Position = view_proj_matrix * vec4(pos, ONE);             \n" +
               "vTexCoord = (rm_Vertex.xy + ONE)/2.0;                                   \n" +
               "vS = t;                       \n" +
            "}";                    
        
    private static final String CHANNEL = "##CHANNEL##";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;                   \n" +
            "uniform sampler2D uColor;                  \n" +
            "uniform sampler2D uShape;                  \n" +
            "uniform float uFetchT;                     \n" +
        
            "varying vec2  vTexCoord;                   \n" +
            "varying float vS;                          \n" +
        
            "void main(void) {                          \n" +
                "vec4 color = texture2D(uColor, vec2(vS, uFetchT));        \n" +
                "vec4 shape = texture2D(uShape, vTexCoord);                \n" +
                "gl_FragColor = vec4(color.rgb, color.a * " + CHANNEL +"); \n" +
            "}";
                  
    
    private static final String U_TIME      = "time_0_X";
    private static final String U_CAMERA_X  = "cameraX";
    private static final String U_CAMERA_Y  = "cameraY";
    private static final String U_VIEWPROJ_TRANSFORM = "view_proj_matrix";
    private static final String U_SYSTEM_HEIGHT = "particleSystemHeight";
    private static final String U_PARTICLE_SPEED   = "particleSpeed";
    private static final String U_PARTICLE_SPREAD  = "particleSpread";
    private static final String U_SYSTEM_SHAPE  = "particleSystemShape";
    private static final String U_PARTICLE_SIZE    = "particleSize";
    private static final String U_COLOR    = "uColor";
    private static final String U_SHAPE    = "uShape";
    private static final String U_FETCH_T    = "uFetchT";
    private static final String A_VERTEX       = "rm_Vertex";
    
    private static int muTime;
    private static int muCameraX;
    private static int muCameraY;
    private static int muViewProj;
    private static int muSystemHeight;
    private static int muParticleSpeed;
    private static int muParticleSpread;
    private static int muSystemShape;
    private static int muParticleSize;
    private static int muColor;
    private static int muShape;
    private static int muFetchT;
    private static final AttribPointer[] maPointers = new AttribPointer[1];
        
     
    private static RenderContext createRenderContext(IGame game, Channel c)
    {    
        final String name = PyramidShapeEmitter.class.getName() + SnareGame.DELIMITER
                + c.toString();
        String fs;
        switch(c) {
        case R: fs = FRAGMENT_SHADER.replaceAll(CHANNEL, "shape.r");
        case G: fs = FRAGMENT_SHADER.replaceAll(CHANNEL, "shape.g");
        case B: fs = FRAGMENT_SHADER.replaceAll(CHANNEL, "shape.b");
        default: fs = FRAGMENT_SHADER.replaceAll(CHANNEL, "shape.a");
        }
        
        final GLState s = new GLState();
        s.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).lock();

        return new RenderContext(game, name, VERTEX_SHADER, fs, s);
    } 
    
    private static final int TEX_UNIT_COLOR = 0;
    private static final int TEX_UNIT_SHAPE = 1;
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource colorTex, BaseTextureResource shapeTex)
    {
        final Material m = new Material(colorTex.mGame);
        m.addTextureUnit(new TextureUnit(TEX_UNIT_COLOR, colorTex, TextureObjOptions.LINEAR_CLAMP));
        m.addTextureUnit(new TextureUnit(TEX_UNIT_SHAPE, shapeTex, TextureObjOptions.LINEAR_CLAMP));
        return m;
    }
    
    private float mHeight = 1f;
    private float mSpeed = 0.5f;
    private float mSpread = 1f;
    private float mSystemShape = 1f;
    private float mSize = 0.1f;
  
    private final DefaultParticleGenerator mGen;
    private float mFetchT;
    
    
    /**
     * Emitter which produces particles continuously in a pyramid-like shape.
     * @param paricleCnt Number of particles
     * @param shapeChannel Color channel which is used for shape fetch
     * @param clock
     */
    public PyramidShapeEmitter(IGame game, int paricleCnt, Channel shapeChannel, IGame.ClockType clock)
    {
        super(createRenderContext(game, shapeChannel),
                new DefaultParticleGenerator(paricleCnt), clock);
        
        // use default particle buffer and pointer
        mGen = (DefaultParticleGenerator) getGenerator();
        if (maPointers[0] == null) {
            maPointers[0] = mGen.getParticlePointer();
        }
    }
  
    public void extractLocations(String programName, int prog) 
    {
        muTime = glGetUniformLocation(prog, U_TIME);
        muCameraX = glGetUniformLocation(prog, U_CAMERA_X);
        muCameraY = glGetUniformLocation(prog, U_CAMERA_Y);
        muViewProj = glGetUniformLocation(prog, U_VIEWPROJ_TRANSFORM);
        muSystemHeight = glGetUniformLocation(prog, U_SYSTEM_HEIGHT);
        muParticleSpeed = glGetUniformLocation(prog, U_PARTICLE_SPEED);
        muParticleSpread = glGetUniformLocation(prog, U_PARTICLE_SPREAD);
        muSystemShape = glGetUniformLocation(prog, U_SYSTEM_SHAPE);
        muParticleSize = glGetUniformLocation(prog, U_PARTICLE_SIZE);
        muColor = glGetUniformLocation(prog, U_COLOR);
        muShape = glGetUniformLocation(prog, U_SHAPE);
        muFetchT = glGetUniformLocation(prog, U_FETCH_T);
        maPointers[0].location = glGetAttribLocation(prog, A_VERTEX);
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass) {
        
        // note: ignore delivered mesh!!
        
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] viewTransformInv = scene.getViewTransformStack().getInv();
                
        //material.bindTextures(); 
        bindBuffers(maPointers); 
        
        glUniformMatrix4fv(muViewProj, 1, false, viewProjTransform, 0);
        glUniform4f(muCameraX, viewTransformInv[0], viewTransformInv[1], viewTransformInv[2], viewTransformInv[3]);
        glUniform4f(muCameraY, viewTransformInv[4], viewTransformInv[5], viewTransformInv[6], viewTransformInv[7]);
       
        glUniform1f(muTime, mTime);
        glUniform1f(muSystemHeight, mHeight);
        glUniform1f(muParticleSpeed, mSpeed);
        glUniform1f(muParticleSpread, mSpread);
        glUniform1f(muSystemShape, mSystemShape);
        glUniform1f(muParticleSize, mSize);
        glUniform1f(muFetchT, mFetchT);
        glUniform1i(muColor, TEX_UNIT_COLOR);
        glUniform1i(muShape, TEX_UNIT_SHAPE);
        
        glDrawElements(GL_TRIANGLE_STRIP, mGen.getParticleIndexCount(), 
                GL_UNSIGNED_SHORT, 0);
        
        maPointers[0].disable();
        return true;
    }
    
    /** Sets t coordinate for texture fetch. */
    public void setColorY(float t) {
        mFetchT = t;
    }
    
    public void setHeight(float height) {
        mHeight = height;
    }
    
    public void setSpeed(float speed) {
        mSpeed = speed;
    }
    
    public void setSpread(float spread) {
        mSpread = spread;
    }
    
    public void setParticleSize(float size) {
        mSize = size;
    }
    
    public void setShape(float shape) {
        mSystemShape = shape;
    }
    
    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mat == null || !mat.hasTextureUnit(TEX_UNIT_COLOR) || 
                !mat.hasTextureUnit(TEX_UNIT_SHAPE))
            throw new IllegalStateException("PyramidShapeEmitter: " +
            		"illegal material");
    }
    
}
