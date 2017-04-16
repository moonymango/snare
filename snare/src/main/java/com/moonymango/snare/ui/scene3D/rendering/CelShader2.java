package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.ui.scene3D.BaseEffect;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.moonymango.snare.opengl.GLES20Trace.glGetUniformLocation;

/**
 * Similar like {@link CelShader} but fragment shader uses a faster texture 
 * lookup rather than reduce colors via calculation:
 *   - dot product from normal and light direction is clamped to [0..1] and
 *     used as s coordinate for texture fetch 
 *   - t coordinate is a uniform and can be set using setFetchT() method
 * 
 * The comic look may be  achieved by using a texture and GL_NEAREST option. 
 * Color of directional light has no impact on result, only the light direction
 * is evaluated in the shader.  
 * Outline is drawn exactly the same as in {@link CelShader}.
 */
public class CelShader2 extends BaseEffect {
    
    private static final String VERTEX_SHADER_FILL =
            "precision lowp float;   \n" +
            "precision lowp int;        \n" +
                    
            "uniform mat4 uViewProjTransform;       \n" +
            "uniform mat4 uViewTransform    ;       \n" +
            "uniform mat4 uNormalTransform;         \n" +
            "uniform vec3 uLightDirection;          \n" +
            "uniform float uFetchT;                         \n" +
            
            "attribute vec4 aPosition;              \n" +
            "attribute vec3 aNormal;                \n" +            
            
            "varying vec2 vTexCoord;                \n" +
            
            "const float ZERO = 0.0;                \n" +
                
            "void main(){                                                                   \n" +
            "   vec3 normal = normalize(vec3(uNormalTransform * vec4(aNormal, ZERO)));       \n" +
            "   vec3 lightDir = normalize(vec3(uViewTransform * vec4(uLightDirection, ZERO)));     \n" +
            "   float fetchS  = 0.5 * (dot(normal, lightDir) + 1.0);                            \n" +
            "   vTexCoord = vec2(fetchS, uFetchT);                                          \n" +
            "   gl_Position = uViewProjTransform * aPosition;                               \n" +
            "}                                                                              \n";                         
    
    private static final String FRAGMENT_SHADER_FILL =
            "precision lowp float;                          \n" +
            "uniform sampler2D uTex;                        \n" +
            
            "varying vec2 vTexCoord;                        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = texture2D(uTex, vTexCoord);  \n" +
            "}                                              \n";            
    
        
    private static RenderContext createRenderContext() 
    {
        final GLState s0 = new GLState();
        s0.enableBackFaceCulling().enableDepth().lock();
        return new RenderContext(CelShader.class.getName(),
                VERTEX_SHADER_FILL, FRAGMENT_SHADER_FILL, s0);
    }
    
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource res, 
            TextureObjOptions options) {
        // effect uses unit 0
        final Material m = new Material();
        m.addTextureUnit(new TextureUnit(0, res, options));
        return m;
    }
    
    
    private static int muNormalTransform;
    private static int muViewProjTransform;
    private static int muViewTransform;
    private static int muLightDirection;
    private static int maPosition;
    private static int maNormal;
    private static int muTex;
    private static int muFetchT;
    
    private float mFetchT = 0;
    
    public CelShader2() {
        super(createRenderContext());
    }

    /** Sets t coordinate for color texture fetch in fragment shader. */
    public void setFetchT(float t) {
        mFetchT = t;
    }
    
    @Override
    public void extractLocations(String programName, int prog) {
        maPosition              = glGetAttribLocation(prog, "aPosition");
        maNormal                = glGetAttribLocation(prog, "aNormal");
        muNormalTransform       = glGetUniformLocation(prog, "uNormalTransform");
        muViewProjTransform     = glGetUniformLocation(prog, "uViewProjTransform");
        muViewTransform         = glGetUniformLocation(prog, "uViewTransform");
        muLightDirection        = glGetUniformLocation(prog, "uLightDirection");
        muTex                   = glGetUniformLocation(prog, "uTex");
        muFetchT                = glGetUniformLocation(prog, "uFetchT");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass)
    {
        float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] normalTransform = scene.getViewTransformStack().getInvTranspose();
        final float[] viewTransform = scene.getCamera().getViewTransform();
        
        // use only the first directional light
        final Light directionalLight = scene.getDirectionalLight(0);
        
        mesh.bindBuffers(maPosition, maNormal, -1, -1);
        
        glUniformMatrix4fv(muViewProjTransform, 1, false, viewProjTransform, 0);
        glUniformMatrix4fv(muViewTransform, 1, false, viewTransform, 0);
        glUniformMatrix4fv(muNormalTransform, 1, false, normalTransform, 0);
        
        if (directionalLight == null) {
            glUniform3f(muLightDirection, 1, 0, 0);
        } else {
            final float[] d = directionalLight.getDirection();
            // reverse light direction so that we get a value in [0..1]
            // when taking dot product in vertex shader code
            glUniform3f(muLightDirection, -d[0], -d[1], -d[2]);
        }
        
        //material.bindTextures();
        glUniform1i(muTex, 0);
        glUniform1f(muFetchT, mFetchT);
        
        glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_SHORT, 
                mesh.getIndexOffset()*Short.SIZE/8);
        
        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maNormal);
        
        return true;
    }
    
    @Override
    protected void check(BaseMesh mesh, Material mat) 
    {
        if (mesh == null || !mesh.hasNormals() || 
                mesh.getDrawMode() != GL_TRIANGLES)
            throw new IllegalStateException("CelShader2: invalid mesh");
        if (mat == null || !mat.hasTextureUnit(0) )
            throw new IllegalStateException("CelShader2: invalid material");         
    }
}
