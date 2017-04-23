package com.moonymango.snare.ui.scene3D.rendering;

import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.ui.scene3D.BaseEffect;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import com.moonymango.snare.ui.scene3D.RenderPass;
import com.moonymango.snare.ui.scene3D.Scene3D;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static com.moonymango.snare.opengl.GLES20Trace.glGetAttribLocation;
import static com.moonymango.snare.opengl.GLES20Trace.glGetUniformLocation;

public class PlainTextureEffect extends BaseEffect {

    private static final String VERTEX_SHADER =
            "precision mediump float;" +
            "uniform mat4 uMatrix;" + 
            "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
    
            "varying vec2 vTexCoord;" +
     
            "void main(){" +
                "vTexCoord = aTexCoord;" +
                "gl_Position = uMatrix  * aPosition;" +
            "}";
    
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +

            "varying vec2 vTexCoord;" +
            "uniform sampler2D uTex;" +
            "uniform vec4 uColor;" +
     
            "void main(){" +
                "gl_FragColor = uColor * texture2D(uTex, vTexCoord);" +
                 
            "}";
    
    /** Attribute location of vertex position. */
    private static int maPosition;
    /** Attribute location of texture coordinates. */
    private static int maTexCoord;
    /** Uniform location of texture */
    private static int muTex;
    /** Uniform location of view/projection matrix. */
    private static int muMatrix;
    /** Uniform location of color. */
    private static int muColor;
    
    private static RenderContext createRenderContext(IGame game)
    {    
        final GLState s = new GLState();
        s.enableDepth().enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).lock();
        return new RenderContext(game,
                PlainTextureEffect.class.getName(),
                VERTEX_SHADER,
                FRAGMENT_SHADER,
                s);
    }
    
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource res, TextureObjOptions options)
    {
        // effect uses unit 0
        final Material m = new Material(res.mGame);
        m.addTextureUnit(new TextureUnit(0, res, options));
        m.setColor(Material.AMBIENT_COLOR_IDX, 1f, 1f, 1f, 1f);
        return m;
    }
    
    public PlainTextureEffect(IGame game) {
        super(createRenderContext(game));
    }

    public void extractLocations(String programName, int prog) {
        maPosition = glGetAttribLocation(prog, "aPosition");
        maTexCoord = glGetAttribLocation(prog, "aTexCoord");
        muMatrix = glGetUniformLocation(prog, "uMatrix");
        muTex = glGetUniformLocation(prog, "uTex");
        muColor = glGetUniformLocation(prog, "uColor");
    }

    @Override
    public boolean render(Scene3D scene, BaseMesh mesh, Material material,
                          GameObj obj, RenderPass pass) {
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        
        // TODO this shouldn't be handled locally in effects but as part of render pass concept
        if (pass == RenderPass.ALPHA) {
            glDepthMask(false);  // when in alpha pass disable depth mask
        }

        mesh.bindBuffers(maPosition, -1, maTexCoord, -1);
        
        glUniformMatrix4fv(muMatrix, 1, false, viewProjTransform, 0);
        
        //material.bindTextures();
        glUniform1i(muTex, 0);
        
        final float[] c = material.getColor(Material.AMBIENT_COLOR_IDX);
        glUniform4f(muColor, c[0], c[1], c[2], c[3]);        
        
        glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_SHORT, 
                mesh.getIndexOffset()*Short.SIZE/8);
        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maTexCoord);

        glDepthMask(true); // TODO remove (see above)
        return true;
    }

    @Override
    protected void check(BaseMesh mesh, Material mat) {
        if (mesh == null || !mesh.hasTexCoords() || 
                mesh.getDrawMode() != GL_TRIANGLES)
            throw new IllegalStateException("PlainTExtureEffect: illegal mesh");
        if (mat == null || !mat.hasTextureUnit(0))
            throw new IllegalStateException("PlainTExtureEffect: " +
            		"illegal material");
    }
    
}
