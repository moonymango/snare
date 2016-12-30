package stachelsau.snare.ui.scene3D.rendering;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import stachelsau.snare.game.Game.ClockType;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.opengl.BufferObj.AttribPointer;
import stachelsau.snare.opengl.GLState;
import stachelsau.snare.ui.scene3D.BaseMesh;
import stachelsau.snare.ui.scene3D.Material;
import stachelsau.snare.ui.scene3D.Scene3D;

public class WeaponParticleEmitter extends BaseDynamicMeshEffect {

    private static final String VERTEX_SHADER =
            "precision mediump float;               \n" +
                    
            "uniform float time_0_X;                \n" +
            "uniform vec4 cameraX;                  \n" +
            "uniform vec4 cameraY;                  \n" +
            "uniform mat4  view_proj_matrix;        \n" +
            "uniform float particleSize;            \n" +
            "uniform float particleSpeed;           \n" +
            "uniform float length;           \n" +
            
            "attribute vec4 rm_Vertex;              \n" +
                       
            "const float ONE = 1.0;                \n" +
            
            "void main(void) {                      \n" +
               // Loop particles
               //"float t = fract(rm_Vertex.w + particleSpeed * time_0_X);    \n" +
               "float t = fract(rm_Vertex.z * particleSpeed * time_0_X);    \n" +
                          
               "vec3 pos;                                                   \n" +
               //"pos.x = particleSpread * s * cos(62.0 * rm_Vertex.z);       \n" +
               //"pos.z = particleSpread * s * cos(163.0 * rm_Vertex.z);      \n" +
               //"pos.y = particleSystemHeight * t;                           \n" +
               "pos.x = t * length;       \n" +
               "pos.z = 0.03 * rm_Vertex.w * sin(32.0 * t + rm_Vertex.w);     \n" +
               "pos.y = 0.03 * rm_Vertex.w * cos(32.0 * t + rm_Vertex.w);                           \n" +
                             
               // Billboard the quads.
               "pos += particleSize * rm_Vertex.z * (rm_Vertex.x * cameraX + rm_Vertex.y * cameraY).xyz;  \n" +
              
               "gl_Position = view_proj_matrix * vec4(pos, ONE);             \n" +
               //"vTexCoord = (rm_Vertex.xy + ONE)/2.0;                                   \n" +
               //"vS = t;                       \n" +
            "}";                    
        
    private static final String FRAGMENT_SHADER =
            "precision mediump float;               \n" +
            
            "void main(void) {                      \n" +
                "gl_FragColor = vec4(1.0, 0.0, 0.0, 0.6); \n" + 
               
            "}";
    
    private static int muTime;
    private static int muCameraX;
    private static int muCameraY;
    private static int muViewProj;
    private static int muParticleSize;
    private static int muParticleSpeed;
    private static int muLength;
    private static final AttribPointer[] maPointers = new AttribPointer[1];
    
    private static RenderContext createRenderContext() {
        final GLState s = new GLState();
        s.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).lock();
        
        final RenderContext c = new RenderContext(
                WeaponParticleEmitter.class.getName(), 
                VERTEX_SHADER, 
                FRAGMENT_SHADER,
                s);
        return c;
    }
    
    private float mSize = 1;
    private float mSpeed = 1;
    private float mLength = 1;
    private final DefaultParticleGenerator mGen;
   
    
    public WeaponParticleEmitter(int particleCnt, float particleSize, 
            float speed, float length) 
    {
        super(createRenderContext(), new DefaultParticleGenerator(particleCnt), 
                ClockType.VIRTUAL);
        
        mSize = particleSize;
        mSpeed = speed;
        mLength = length;
        
        mGen = (DefaultParticleGenerator) getGenerator();
        if (maPointers[0] == null)
            maPointers[0] = mGen.getParticlePointer();
    }

    @Override
    public void extractLocations(String programName, int prog) {
        muTime = glGetUniformLocation(prog, "time_0_X");
        muCameraX = glGetUniformLocation(prog, "cameraX");
        muCameraY = glGetUniformLocation(prog, "cameraY");
        muViewProj = glGetUniformLocation(prog, "view_proj_matrix");
        muParticleSize = glGetUniformLocation(prog, "particleSize");
        muParticleSpeed = glGetUniformLocation(prog, "particleSpeed");
        muLength = glGetUniformLocation(prog, "length");
        maPointers[0].location = glGetAttribLocation(prog, "rm_Vertex");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material, 
            GameObj obj) {
        
        if (mTime > 1) mTime = 0;
        
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] viewTransformInv = scene.getViewTransformStack().getInv();
         
        bindBuffers(maPointers); 
        
        glUniformMatrix4fv(muViewProj, 1, false, viewProjTransform, 0);
        glUniform4f(muCameraX, viewTransformInv[0], viewTransformInv[1], viewTransformInv[2], viewTransformInv[3]);
        glUniform4f(muCameraY, viewTransformInv[4], viewTransformInv[5], viewTransformInv[6], viewTransformInv[7]);
       
        glUniform1f(muTime, mTime);
        glUniform1f(muParticleSpeed, mSpeed);
        glUniform1f(muParticleSize, mSize);
        glUniform1f(muLength, mLength);
                
        glDrawElements(GL_TRIANGLE_STRIP, mGen.getParticleIndexCount(), 
                GL_UNSIGNED_SHORT, 0);
        
        maPointers[0].disable();
        return true;
    }

    public void setParticleSize(float size) {mSize = size;}
    public void setSpeed(float speed) {mSpeed = speed;}

    @Override
    protected void check(BaseMesh mesh, Material mat) {
        // no dependencies to mesh or material
    }

    
}
