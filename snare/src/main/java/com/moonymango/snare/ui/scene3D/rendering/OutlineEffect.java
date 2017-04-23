package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.ui.scene3D.BaseEffect;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;
import com.moonymango.snare.util.MatrixAF;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.moonymango.snare.opengl.GLES20Trace.glGetUniformLocation;

/**
 * Draws the outline of an object.
 * The effect is achieved by scaling the object a bit larger and drawing the backside faces
 * in a plain color.
 */
public class OutlineEffect extends BaseEffect {
    
    public static final String VERTEX_SHADER_OUTLINE =
            "precision highp float;   \n" +
                    
            "uniform mat4 uViewProjTransform;       \n" +
            "uniform mat4 uScale;                   \n" +
            "attribute vec4 aPosition;              \n" +

            "void main(){                                       \n" +
            "   gl_Position = uScale * aPosition;               \n" +
            "   gl_Position = uViewProjTransform * gl_Position; \n" +
            "   // increase depth a lil bit to prevent z fighting with frontside faces \n" +
            "   gl_Position.z = gl_Position.z + 1.0/1024.0 * abs(gl_Position.w); \n" +
            "}                                                  \n";                    
        
    public static final String FRAGMENT_SHADER_OUTLINE = 
            "precision highp float;       \n" +
            "uniform vec4 uColor;        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = uColor;                      \n" +
            "}                                              \n";              
    
        
    private static RenderContext createRenderContext(IGame game)
    { 
        final GLState s = new GLState();
        s.enableFrontFaceCulling().enableDepth().lock();

        return new RenderContext(game, OutlineEffect.class.getName(), VERTEX_SHADER_OUTLINE, FRAGMENT_SHADER_OUTLINE, s);
    }
    
    private static int muViewProjTransformOutline;
    private static int muScaleOutline;
    private static int maPositionOutline;
    private static int muColorOutline;
    
    // intermediate storage
    private float[] mScale = new float[16];

    public OutlineEffect(IGame game) {
        super(createRenderContext(game));

        MatrixAF.setIdentityM(mScale, 0);
    }

    @Override
    public void extractLocations(String programName, int prog) {
        maPositionOutline           = glGetAttribLocation(prog, "aPosition");
        muScaleOutline              = glGetUniformLocation(prog, "uScale");
        muColorOutline              = glGetUniformLocation(prog, "uColor");
        muViewProjTransformOutline  = glGetUniformLocation(prog, "uViewProjTransform");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass) {

        // copy scale value from material to matrix
        final float[] outlineScale = material.getOutlineScale();
        mScale[0] = outlineScale[0];
        mScale[5] = outlineScale[1];
        mScale[10] = outlineScale[2];

        mesh.bindBuffers(maPositionOutline, -1, -1, -1);

        glUniformMatrix4fv(muScaleOutline, 1, false, mScale, 0);
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        glUniformMatrix4fv(muViewProjTransformOutline, 1, false, viewProjTransform, 0);
        final float[] outlineColor = material.getColor(Material.OUTLINE_COLOR_IDX);
        glUniform4f(muColorOutline, outlineColor[0], outlineColor[1], 
                outlineColor[2], outlineColor[3]);
        
        glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_SHORT, 
                mesh.getIndexOffset()*Short.SIZE/8);
    

        return true;
    }
    
    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mesh == null || mesh.getDrawMode() != GL_TRIANGLES)
            throw new IllegalStateException("OutlineEffect: illegal mesh");
        if (mat == null || mat.getColor(Material.OUTLINE_COLOR_IDX) == null)
            throw new IllegalStateException("OutlineEffect: illegal mat");
    }
}
