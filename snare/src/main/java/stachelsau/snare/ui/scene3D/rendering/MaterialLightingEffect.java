package stachelsau.snare.ui.scene3D.rendering;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static stachelsau.snare.opengl.GLES20Trace.glGetAttribLocation;
import static stachelsau.snare.opengl.GLES20Trace.glGetUniformLocation;
import stachelsau.snare.game.GameObj;
import stachelsau.snare.opengl.GLState;
import stachelsau.snare.opengl.TextureObj.TextureUnit;
import stachelsau.snare.opengl.TextureObjOptions;
import stachelsau.snare.res.texture.BaseTextureResource;
import stachelsau.snare.ui.scene3D.BaseEffect;
import stachelsau.snare.ui.scene3D.BaseMesh;
import stachelsau.snare.ui.scene3D.Material;
import stachelsau.snare.ui.scene3D.Scene3D;

/**
 * Draws object using material properties and lights.
 */
public class MaterialLightingEffect extends BaseEffect {

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
    protected static int maPosition;
    /** Attribute location of texture coordinates. */
    protected static int maTexCoord;
    /** Uniform location of texture */
    protected static int muTex;
    /** Uniform location of view/projection matrix. */
    protected static int muMatrix;
    /** Uniform location of color. */
    protected static int muColor;
    
    private static RenderContext createRenderContext() 
    {    
        final GLState s = new GLState();
        s.enableDepth().enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).lock();
        final RenderContext c = new RenderContext(
                MaterialLightingEffect.class.getName(), 
                VERTEX_SHADER, 
                FRAGMENT_SHADER, 
                s);
        return c;
    }
    
    /** Creates {@link Material} object matching the effect. */
    public static Material makeMaterial(BaseTextureResource res, 
            TextureObjOptions options) {
        // effect uses unit 0
        final Material m = new Material();
        m.addTextureUnit(new TextureUnit(0, res, options));
        return m;
    }
    
    public MaterialLightingEffect() {
        super(createRenderContext());
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
            GameObj obj) {
        final float[] viewProjTransform = scene.getModelViewProjMatrix();
        
        //useProgram(0);
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
