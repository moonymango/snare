package stachelsau.snare.ui.scene3D.rendering;

import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static stachelsau.snare.opengl.GLES20Trace.glGetUniformLocation;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.opengl.GLState;
import stachelsau.snare.ui.scene3D.BaseEffect;
import stachelsau.snare.ui.scene3D.BaseMesh;
import stachelsau.snare.ui.scene3D.Material;
import stachelsau.snare.ui.scene3D.Scene3D;
import stachelsau.snare.util.MatrixAF;
import stachelsau.snare.util.MatrixStack;

/**
 * Draws the outline of an object.
 */
public class OutlineEffect extends BaseEffect {
    
    public static final String VERTEX_SHADER_OUTLINE =
            "precision mediump float;   \n" +
                    
            "uniform mat4 uViewProjTransform;       \n" +
            "attribute vec4 aPosition;              \n" +
                
            "void main(){                                       \n" +
            "   gl_Position = uViewProjTransform * aPosition;   \n" +
            "}                                                  \n";                    
        
    public static final String FRAGMENT_SHADER_OUTLINE = 
            "precision lowp float;       \n" +
            "uniform vec4 uColor;        \n" +
            
            "void main(){                                   \n" +
            "   gl_FragColor = uColor;                      \n" +
            "}                                              \n";              
    
        
    private static final RenderContext createRenderContext() 
    { 
        final GLState s = new GLState();
        s.enableFrontFaceCulling().enableDepth().lock();
        final RenderContext c = new RenderContext(OutlineEffect.class.getName(), 
                VERTEX_SHADER_OUTLINE, 
                FRAGMENT_SHADER_OUTLINE, s);
        return c;
    }
    
    private static int muViewProjTransformOutline;
    private static int maPositionOutline;
    private static int muColorOutline;
    
    // intermediate storage
    private float[] mMat0 = new float[16];
    private float[] mMat1 = new float[16];
    private float[] mVec = new float[4];
    
    public OutlineEffect() {
        super(createRenderContext());
    }

    @Override
    public void extractLocations(String programName, int prog) {
        maPositionOutline           = glGetAttribLocation(prog, "aPosition");
        muColorOutline              = glGetUniformLocation(prog, "uColor");
        muViewProjTransformOutline  = glGetUniformLocation(prog, "uViewProjTransform");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material, 
            GameObj obj) {
        
        // Produce outline by scaling the object a bit larger and drawing
        // in plain outline color. Scaling object requires recalculation
        // of the stack's top matrix
        
        final MatrixStack stack = scene.getViewTransformStack();
        // save copy of top matrix
        final float[] top = stack.getTop();
        for (int i = 0; i < 16; i++) {
            mMat0[i] = top[i];
        }
        stack.popMatrix();
        
        // set up new scaled matrix
        final float[] scale = obj.getScale();
        final float[] outlineScale = material.getOutlineScale();
        mVec[0] = scale[0] * outlineScale[0];
        mVec[1] = scale[1] * outlineScale[1];
        mVec[2] = scale[2] * outlineScale[2];
        mVec[3] = scale[3];
        MatrixAF.local2World(mMat1, obj.getPosition(), mVec, obj.getRotation());
        stack.pushMatrix(mMat1);
                
        //useProgram(1);
        mesh.bindBuffers(maPositionOutline, -1, -1, -1);
        
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        glUniformMatrix4fv(muViewProjTransformOutline, 1, false, viewProjTransform, 0);
        final float[] outlineColor = material.getColor(Material.OUTLINE_COLOR_IDX);
        glUniform4f(muColorOutline, outlineColor[0], outlineColor[1], 
                outlineColor[2], outlineColor[3]);
        
        glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_SHORT, 
                mesh.getIndexOffset()*Short.SIZE/8);
    
        // restore matrix stack
        stack.popMatrix();
        stack.pushMatrix(mMat0);    
        
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
