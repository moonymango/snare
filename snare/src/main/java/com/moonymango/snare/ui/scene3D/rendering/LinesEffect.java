package com.moonymango.snare.ui.scene3D.rendering;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLineWidth;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.ui.scene3D.BaseEffect;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;

public class LinesEffect extends BaseEffect {

    private static final String VERTEX_SHADER =
            "precision mediump float;   \n" +
            "precision lowp int;        \n" +
                    
            "uniform mat4 uViewProjTransform;       \n" +            
            "uniform vec4 uColor;                   \n" +
            
            "attribute vec4 aPosition;        \n" +         
            
            "varying vec4 vColor;                   \n" +
                
            "void main(){                                                                   \n" +
            "   vColor = uColor;                                                            \n" +
            "   gl_Position = uViewProjTransform * aPosition;                               \n" +
            "}                                                                              \n";                    
        
    private static final String FRAGMENT_SHADER = 
            "precision lowp float;      \n" +
            "precision lowp int;        \n" +
                    
            "varying vec4 vColor;        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = vColor;                      \n" +
            "}                                              \n";      
    
    private static final String A_POSITION = "aPosition";
    private static final String U_VIEWPROJ_TRANSFORM = "uViewProjTransform";
    private static final String U_COLOR = "uColor";
    
    private static int muViewProjTransform;
    private static int muColor;
    private static int maPosition;
    
    
    private static RenderContext createRenderContext() {
        final GLState s = new GLState();
        s.enableDepth().lock();

        return new RenderContext(
                LinesEffect.class.getName(), VERTEX_SHADER, FRAGMENT_SHADER, s);
    }
    
    private final float mLineWidth;
    
    
    public LinesEffect() {
        this(1);
    }
    
    public LinesEffect(float width) {
        super(createRenderContext());
        mLineWidth = width;
    }
    
    public void extractLocations(String programName, int prog) {
        maPosition = glGetAttribLocation(prog, A_POSITION);
        muViewProjTransform = glGetUniformLocation(prog, U_VIEWPROJ_TRANSFORM);
        muColor = glGetUniformLocation(prog, U_COLOR);

    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass) {
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        
        glLineWidth(mLineWidth); //FIXME add this property to GlState
        
        //useProgram(0);
        mesh.bindBuffers(maPosition, -1, -1, -1);
        
        glUniformMatrix4fv(muViewProjTransform, 1, false, viewProjTransform, 0);
        
        float[] c = material.getColor(Material.LINE_COLOR_IDX);
        glUniform4f(muColor, c[0], c[1], c[2], c[3]);   
        glDrawElements(GL_LINES, mesh.getIndexCount(), GL_UNSIGNED_SHORT, 
                mesh.getIndexOffset()*Short.SIZE/8);
        glDisableVertexAttribArray(maPosition);
        return true;
    }

    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mesh == null || mesh.getDrawMode() != GL_LINES)
            throw new IllegalStateException("LinesEffect: illegal mesh");
        if (mat == null || mat.getColor(Material.LINE_COLOR_IDX) == null)
            throw new IllegalStateException("LinesEffect: illegal material");
    }

    
    
}
