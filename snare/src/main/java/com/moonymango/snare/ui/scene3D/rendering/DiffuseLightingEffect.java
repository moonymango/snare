package com.moonymango.snare.ui.scene3D.rendering;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.ui.scene3D.BaseEffect;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Light;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.Scene3D;

/**
 * Simple diffuse and ambient lighting. Supports only one ambient and one
 * directional light source (the first lights in the scene's lists are used)
 */
public class DiffuseLightingEffect extends BaseEffect {
  
    private static final String VERTEX_SHADER =
            "precision mediump float;   \n" +
            "precision lowp int;        \n" +
                    
            "uniform mat4 uViewProjTransform;       \n" +
            "uniform mat4 uViewTransform    ;       \n" +
            "uniform mat4 uNormalTransform;         \n" +
            "uniform vec4 uDiffuseReflection;       \n" +
            "uniform vec4 uAmbientReflection;       \n" +
            "uniform vec4 uLightColor;              \n" +
            "uniform vec4 uAmbientLightColor;       \n" +
            "uniform vec3 uLightDirection;          \n" +
            
            "attribute vec4 aPosition;              \n" +
            "attribute vec3 aNormal;                \n" +         
            
            "varying vec4 vColor;                   \n" +
            
            "const float ZERO = 0.0;                \n" +
                
            "void main(){                                                                   \n" +
            "   vec3 normal = normalize(vec3(uNormalTransform * vec4(aNormal, ZERO)));       \n" +
            "   vec3 lightDir = normalize(vec3(uViewTransform * vec4(uLightDirection, ZERO)));     \n" +
            "   float NdotL  = max(dot(normal, lightDir), ZERO);                             \n" +
            "   vColor = NdotL * uDiffuseReflection * uLightColor;                          \n" +
            "   vColor += uAmbientReflection * uAmbientLightColor;                          \n" +
            "   gl_Position = uViewProjTransform * aPosition;                               \n" +
            "}                                                                              \n";                    
        
    private static final String FRAGMENT_SHADER = 
            "precision lowp float;      \n" +
            "precision lowp int;        \n" +
                    
            "varying vec4 vColor;        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = vColor;                      \n" +
            "}                                              \n";      
     
    private static RenderContext createRenderContext() {
        final GLState s = new GLState();
        s.enableDepth().enableBackFaceCulling().lock();
        return new RenderContext(
                DiffuseLightingEffect.class.getName(),
                VERTEX_SHADER,
                FRAGMENT_SHADER,
                s);
    }
    
    
    
    private static final String A_POSITION = "aPosition";
    private static final String A_NORMAL = "aNormal";
    private static final String U_NORMAL_TRANSFORM = "uNormalTransform";
    private static final String U_VIEWPROJ_TRANSFORM = "uViewProjTransform";
    private static final String U_VIEW_TRANSFORM = "uViewTransform";
    private static final String U_DIFFUSE_REFLECTION = "uDiffuseReflection";
    private static final String U_AMBIENT_REFLECTION = "uAmbientReflection";
    private static final String U_LIGHT_COLOR = "uLightColor";
    private static final String U_AMBIENT_LIGHT_COLOR = "uAmbientLightColor";
    private static final String U_LIGHT_DIR = "uLightDirection";
    
    private static int muNormalTransform;
    private static int muViewProjTransform;
    private static int muViewTransform;
    private static int muDiffuseReflection;
    private static int muAmbientReflection;
    private static int muLightColor;
    private static int muAmbientLightColor;
    private static int muLightDirection;
    private static int maPosition;
    private static int maNormal;
    
        
    public DiffuseLightingEffect() {
        super(createRenderContext());
    }
    
    public void extractLocations(String programName, int prog) {
        maPosition = glGetAttribLocation(prog, A_POSITION);
        maNormal = glGetAttribLocation(prog, A_NORMAL);
        muNormalTransform = glGetUniformLocation(prog, U_NORMAL_TRANSFORM);
        muViewProjTransform = glGetUniformLocation(prog, U_VIEWPROJ_TRANSFORM);
        muViewTransform = glGetUniformLocation(prog, U_VIEW_TRANSFORM);
        muLightDirection = glGetUniformLocation(prog, U_LIGHT_DIR);
        muDiffuseReflection = glGetUniformLocation(prog, U_DIFFUSE_REFLECTION);
        muAmbientReflection = glGetUniformLocation(prog, U_AMBIENT_REFLECTION);
        muLightColor = glGetUniformLocation(prog, U_LIGHT_COLOR);
        muAmbientLightColor = glGetUniformLocation(prog, U_AMBIENT_LIGHT_COLOR);
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material, 
            GameObj obj) {
        float[] v;
        final float[] normalTransform = scene.getViewTransformStack().getInvTranspose();
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] viewTransform = scene.getCamera().getViewTransform();
        
        // use only the first ambient and directional lights
        final Light directionalLight = scene.getDirectionalLight(0);
        final Light ambientLight = scene.getAmbientLight(0);
        
        //useProgram(0);
        mesh.bindBuffers(maPosition, maNormal, -1, -1);
        
        glUniformMatrix4fv(muViewProjTransform, 1, false, viewProjTransform, 0);
        glUniformMatrix4fv(muViewTransform, 1, false, viewTransform, 0);
        glUniformMatrix4fv(muNormalTransform, 1, false, normalTransform, 0);
        
        if (directionalLight == null) {
            glUniform3f(muLightDirection, 1, 0, 0);
            glUniform4f(muLightColor, 0, 0, 0, 0);
        } else {
            v = directionalLight.getDirection();
            // reverse light direction so that we get a value in [0..1]
            // when taking dot product in vertex shader code
            glUniform3f(muLightDirection, -v[0], -v[1], -v[2]);
            v = directionalLight.getColor();
            glUniform4f(muLightColor, v[0], v[1], v[2], 1);
        }
        
        if (ambientLight == null) {
            glUniform4f(muAmbientLightColor, 0, 0, 0, 0);
        } else {
            v = ambientLight.getColor();
            glUniform4f(muAmbientLightColor, v[0], v[1], v[2], 1);
        }
        
        v = material.getColor(Material.DIFFUSE_COLOR_IDX);
        glUniform4f(muDiffuseReflection, v[0], v[1], v[2], v[3]);
        v = material.getColor(Material.AMBIENT_COLOR_IDX);
        glUniform4f(muAmbientReflection, v[0], v[1], v[2], v[3]);
                
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
            throw new IllegalStateException("DiffuseLightningEffect: " +
            		"invalid mesh");
        if (mat == null || mat.getColor(Material.AMBIENT_COLOR_IDX) == null ||
                mat.getColor(Material.DIFFUSE_COLOR_IDX) == null)
            throw new IllegalStateException("DiffuseLightningEffect: " +
            		"invalid material");
    }

}
