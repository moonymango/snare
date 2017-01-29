package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.opengl.GLState;
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
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class CelShader extends BaseEffect {
    
    private static final String LEVELS = "##LEVELS##";
    
    public static final String VERTEX_SHADER_FILL =
          //  "precision highp float;   \n" +
          //  "precision highp int;        \n" +
                    
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
            "   float NdotL  = max(dot(normal, lightDir), 0.2);                             \n" +
            "   vColor = NdotL * uDiffuseReflection * uLightColor;                          \n" +
            "   vColor += uAmbientReflection * uAmbientLightColor;                          \n" +
            "   gl_Position = uViewProjTransform * aPosition;                               \n" +
            "}                                                                              \n";                         
    
    // code number of levels hard into shader for performance reasons,
    // still 2 multiplications are quite expensive!! (at least in 2013)
    public static final String FRAGMENT_SHADER_FILL = 
            "precision highp float;       \n" +
            "varying vec4 vColor;        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = floor(vColor*"+LEVELS+") * 1.0/"+LEVELS+"; \n" +
            "}                                              \n";            
    
    // FIXME prioC: not possible to have cel shaders with different number of 
    //              levels in an app because  number of levels is compiled into 
    //              shader. So we get multiple programs and need a set of 
    //              variables to hold locations for each of them
    
     
    private static RenderContext createRenderContext(int levels) 
    {
        final String vs = VERTEX_SHADER_FILL.replaceAll(LEVELS, Float.toString(levels));
        final String fs = FRAGMENT_SHADER_FILL.replaceAll(LEVELS, Float.toString(levels));
        
        final GLState s0 = new GLState();
        s0.enableBackFaceCulling().enableDepth().lock();

        return new RenderContext(CelShader.class.getName(),
                vs, fs, s0);
    }
    
    private static int muViewProjTransform;
    private static int muViewTransform;
    private static int muNormalTransform;
    private static int muDiffuseReflection;
    private static int muAmbientReflection;
    private static int muLightColor;
    private static int muAmbientLightColor;
    private static int muLightDirection;
    private static int maPosition;
    private static int maNormal;
    
    
    public CelShader() {
        this(3);
    }
    
    public CelShader(int levels) {
        super(createRenderContext(levels));
    }

    public void extractLocations(String programName, int prog) {
        maPosition = glGetAttribLocation(prog, "aPosition");
        maNormal = glGetAttribLocation(prog, "aNormal");
        muNormalTransform = glGetUniformLocation(prog, "uNormalTransform");
        muViewProjTransform = glGetUniformLocation(prog, "uViewProjTransform");
        muViewTransform = glGetUniformLocation(prog, "uViewTransform");
        muLightDirection = glGetUniformLocation(prog, "uLightDirection");
        muDiffuseReflection = glGetUniformLocation(prog, "uDiffuseReflection");
        muAmbientReflection = glGetUniformLocation(prog, "uAmbientReflection");
        muLightColor = glGetUniformLocation(prog, "uLightColor");
        muAmbientLightColor = glGetUniformLocation(prog, "uAmbientLightColor");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass)
    {    
        float[] viewProjTransform = scene.getModelViewProjMatrix();
        final float[] normalTransform = scene.getViewTransformStack().getInvTranspose();
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
            final float[] d = directionalLight.getDirection();
            // reverse light direction so that we get a value in [0..1]
            // when taking dot product in vertex shader code
            glUniform3f(muLightDirection, -d[0], -d[1], -d[2]);
            final float [] c = directionalLight.getColor();
            glUniform4f(muLightColor, c[0], c[1], c[2], 1);
        }
        final float[] dr = material.getColor(Material.DIFFUSE_COLOR_IDX);
        glUniform4f(muDiffuseReflection, dr[0], dr[1], dr[2], dr[3]);
        
        if (ambientLight == null) {
            glUniform4f(muAmbientLightColor, 0, 0, 0, 0);
        } else {
            final float[] c = ambientLight.getColor();
            glUniform4f(muAmbientLightColor, c[0], c[1], c[2], 1);
        }
        final float[] ar = material.getColor(Material.AMBIENT_COLOR_IDX);
        glUniform4f(muAmbientReflection, ar[0], ar[1], ar[2], ar[3]);
        
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
            throw new IllegalStateException("CelShader: invalid mesh");
        if (mat == null || mat.getColor(Material.OUTLINE_COLOR_IDX) == null)
            throw new IllegalStateException("CelShader: invalid material");
    }

}
