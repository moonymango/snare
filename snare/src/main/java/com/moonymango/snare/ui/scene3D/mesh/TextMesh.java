package com.moonymango.snare.ui.scene3D.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.opengl.TextureObj.TextureUnit;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.scene3D.BaseMesh;
import com.moonymango.snare.ui.scene3D.Material;
import android.opengl.GLES20;
 
public class TextMesh extends BaseMesh {

    private final BaseFont mFont;
    private String mText;
    private final Material mMat = new Material();
    
    public TextMesh(BaseFont font, String text) {
        super(TextMesh.class.getName() + Game.DELIMITER + Game.get().getRandomString(),
                false, false);
        mFont = font;
        mText = text;
        mMat.addTextureUnit(new TextureUnit(0, font, TextureObjOptions.LINEAR_REPEAT));
        
    }
    
    public boolean hasNormals() {
        return false;
    }
    
    public int getDrawMode() {
        return GLES20.GL_TRIANGLES;
    }

    public boolean hasTexCoords() {
        return true;
    }
    
    @Override
    public boolean hasColor() {
        return false;
    }

    @Override
    protected FloatBuffer getVertices() {
        final int vertexCnt = BaseFont.getVertexAttrBufferLen(mText.length());
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCnt * Float.SIZE/8); 
        vbb.order(ByteOrder.nativeOrder());
        final FloatBuffer fb = vbb.asFloatBuffer();
        mFont.fillVertexAttrBuffer(mText, fb);
       
        return fb;
    }

    @Override
    protected ShortBuffer getIndices() {
        final int indexCnt = BaseFont.getIndexBufferLen(mText.length());  
        ByteBuffer ebb = ByteBuffer.allocateDirect(indexCnt * Short.SIZE/8);  
        ebb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = ebb.asShortBuffer();
        mFont.initIndexBuffer(sb);
        return sb;
    }

    @Override
    protected int getStride() {
        return BaseFont.STRIDE;
    }

    @Override
    protected int getNormalOffset() {
        return 0;
    }

    @Override
    protected int getTexOffset() {
        return BaseFont.TEX_OFFSET;
    }
    
    @Override
    protected int getColorOffset() {
        return 0;
    }

    @Override
    public int getIndexCount() {
        return BaseFont.getIndexBufferLen(mText.length());
    }

    @Override
    public int getIndexOffset() {
        return 0;
    }

    /** Returns material with font texture at tex unit 0. */
    @Override
    public Material getMaterial() {
        return mMat;
    }
   
}
