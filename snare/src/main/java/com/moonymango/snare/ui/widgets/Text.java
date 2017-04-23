package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.game.SnareGame;
import com.moonymango.snare.opengl.BufferObj;
import com.moonymango.snare.opengl.BufferObj.IBufferConfigurationSetup;
import com.moonymango.snare.opengl.BufferObj.IBufferDataProvider;
import com.moonymango.snare.opengl.BufferObj.IBufferUpdateSetup;
import com.moonymango.snare.opengl.BufferObj.Target;
import com.moonymango.snare.opengl.GLObjDescriptor;
import com.moonymango.snare.opengl.GLObjDescriptor.GLObjType;
import com.moonymango.snare.opengl.GLState;
import com.moonymango.snare.opengl.ProgramObj;
import com.moonymango.snare.opengl.TextureObj;
import com.moonymango.snare.opengl.TextureObjOptions;
import com.moonymango.snare.res.texture.BaseTextureResource;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.PlayerGameView;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Widget that displays a character sequence using bitmap font textures. The
 * texture's color channels are expected to represent the following: 
 * R = not used, 
 * G = glyph, 
 * B = glyph including outline, 
 * A = not used
 * 
 * {@link Text} uses vertex and index GL buffer objects. The index buffer is
 * shared by all instances, the vertex buffer is specific to an instance.
 * The text widget also instantiates a {@link Buffer} at construction, which 
 * is used to update it's GL vertex buffer object. Thus changing the text 
 * content is allocation free. The size of this pre-allocated {@link Buffer} 
 * (and therefore the max. number of characters) may be specified via  
 * constructor parameter.
 */
public class Text extends BaseTouchWidget implements IBufferDataProvider {

    // ---------------------------------------------------------
    // static
    // ---------------------------------------------------------
    public static final int MAX_TEXT_LENGTH = 512;
    
    private static final String VERTEX_SHADER = 
            "precision lowp float;"
            + "uniform mat4 uMatrix;" + "attribute vec4 aPosition;"
            + "attribute vec2 aTexCoord;" + "varying vec2 vTexCoord;"

            + "void main(){" 
            + "vTexCoord = aTexCoord;"
            + "gl_Position = uMatrix  * aPosition;" + "}";

    private static final String FRAGMENT_SHADER_TEX = 
            "precision lowp float;"
            + "varying vec2 vTexCoord;"
            + "uniform sampler2D uTex;"
            + "uniform vec4 uColor;"
            + "uniform vec4 uOutlineColor;"
        
            + "void main(){"
            + "vec4 font = texture2D(uTex, vTexCoord);"
            // glyph is in green channel
            + "vec4 glyph = font.g * uColor;"
            // subtract glyph from blue channel to get raw outline 
            + "vec4 outline = (font.b - font.g) * uOutlineColor;"
            + "gl_FragColor = glyph + outline;"
        
            + "}";    

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
    /** Uniform location of outline color. */
    private static int muOutlineColor;
    
    private static final GLState sGlState = new GLState();

    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    private final GLObjDescriptor mProgramDescr;
    private final GLObjDescriptor mTextureObjDescr;
    private final TextureObjOptions mTextureOptions;

    private ProgramObj mProgram;
    private TextureObj mTextureObj;
    private final BaseTextureResource mTextureResource;

    private final BaseFont mFont;

    private final FloatBuffer mVertexAttrBuf;
    /** Maximal number of characters the vertex buffer can accommodate. */
    private final int mMaxNumChars;
    /** Number of characters actually included in vertex buffer. */
    private int mNumChar;

    private final GLObjDescriptor mVertexAttrBufferObjDescr;
    private final GLObjDescriptor mIndexBufferObjDescr;
    private BufferObj mVertexAttrBufferObj;
    private BufferObj mIndexBufferObj;

    private int mTextSize = 26;
    private String mText;
    private int mIndexCnt;
    private float mVertexSpanX;
    private float mVertexSpanY;
    private float mVertexLeftX;
    private float mVertexTopY;
    private PositionAlignment mAlignment;
    
    private final float[] mOutlineColor = {0, 0, 0, 1};
    private ColorWrapper mOutlineColorPalette;

    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    /**
     * Creates text widget.
     * @param font Bitmap font
     * @param text
     * @param alignment Widget alignment
     * @param maxChars Buffer size. If less than text length, then the text
     *              will be truncated.
     * 
     */
    public Text(BaseFont font, String text, PositionAlignment alignment, TouchSetting setting, int maxChars)
    {
        super(font.mGame, setting);
        mTextureResource = font;
        mTextureOptions = TextureObjOptions.LINEAR_CLAMP;
        mTextureObjDescr = new GLObjDescriptor(mGame, mTextureResource.getName(), GLObjType.TEXTURE);
        mProgramDescr = new GLObjDescriptor(mGame, Text.class.getName() + ".program", GLObjType.PROGRAM);

        mFont = font;

        // allocate vertex and index buffers
        if (maxChars > MAX_TEXT_LENGTH && maxChars < 1) {
            throw new IllegalArgumentException("Invalid maxChars.");
        }
        mMaxNumChars = maxChars;
        final int vertexCnt = BaseFont.getVertexAttrBufferLen(maxChars);
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCnt * Float.SIZE / 8);
        vbb.order(ByteOrder.nativeOrder());
        mVertexAttrBuf = vbb.asFloatBuffer();

        // GL buffer objects (vertex buffer is unique to each instance
        // because they probably contain different text. Index buffer is
        // global.
        final String s = SnareGame.get().getRandomString();
        mVertexAttrBufferObjDescr = new GLObjDescriptor(mGame, Text.class.getName() +".vertices_" + s, GLObjType.BUFFER);
        mIndexBufferObjDescr = new GLObjDescriptor(mGame, Text.class.getName() + ".indices", GLObjType.BUFFER);

        mAlignment = alignment != null ? alignment
                : PositionAlignment.CENTERED_XY;
        
        setText(text == null ? "" : text); // this also sets up vertex buffer
        
        
        sGlState.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Create text widget.
     * @param font Font.
     * @param text Text to display.
     * @param maxChars Buffer size. If less than text length, then the text
     *              will be truncated.
     */
    public Text(BaseFont font, String text, TouchSetting setting, int maxChars)
    {
        this(font, text, PositionAlignment.CENTERED_XY, setting, maxChars);
    }
    
    /**
     * Creates text widget. Buffer size is set to number of characters in text.
     * @param font Bitmap font.
     * @param text Text to display.
     */
    public Text(BaseFont font, String text, TouchSetting setting)
    {
        this(font, text, PositionAlignment.CENTERED_XY, setting, text.length());
    }

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public Text setTextSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException(
                    "Text size must not be less than 1.");
        }
        mTextSize = size;
        final float scale = ((float) mTextSize) / mFont.getLineHeight();
        super.setScale(scale, scale);
        return this;
    }

    @Override
    public void onAttachToScreen(PlayerGameView view, int screenWidth,
            int screenHeight) {
        mProgram = (ProgramObj) mProgramDescr.getHandle();
        if (!mProgram.isConfigured()) {
            mProgram.configure(VERTEX_SHADER, FRAGMENT_SHADER_TEX, this);
        }
        // texture
        mTextureObj = (TextureObj) mTextureObjDescr.getHandle();
        if (!mTextureObj.isConfigured()) {
            mTextureObj.configure(mTextureResource, mTextureOptions);
        }
        
        // VBOs
        if (mVertexAttrBufferObj == null) {
            mVertexAttrBufferObj = (BufferObj) mVertexAttrBufferObjDescr
                    .getHandle();
        }
    
        if (!mVertexAttrBufferObj.isConfigured()) {
            mVertexAttrBufferObj.configure(this);
        } else {
            mVertexAttrBufferObj.update(1);
        }
        
        if (mIndexBufferObj == null) {
            mIndexBufferObj = (BufferObj) mIndexBufferObjDescr.getHandle();
        }
        if (!mIndexBufferObj.isConfigured()) {
            mIndexBufferObj.configure(this);
        }
        
        super.onAttachToScreen(view, screenWidth, screenHeight);
    }

    @Override
    public void onDetachFromScreen() {
        // program
        mProgramDescr.releaseHandle(mProgram);
        mProgram = null;
        // texture
        mTextureObjDescr.releaseHandle(mTextureObj);
        mTextureObj = null;

        //VBOs
        mVertexAttrBufferObjDescr.releaseHandle(mVertexAttrBufferObj);
        mVertexAttrBufferObj = null;
        mIndexBufferObjDescr.releaseHandle(mIndexBufferObj);
        mIndexBufferObj = null;
        
        super.onDetachFromScreen();
    }

    @Override
    public Text setScale(float x, float y) {
        mTextSize = (int) (y * mFont.getLineHeight());
        super.setScale(x, y);
        return this;
    }

    public Text setText(String text) {
        mText = text.length() <= mMaxNumChars ? text : 
            text.substring(0, mMaxNumChars-1);
        setupVertexAttrBufferObj();

        // size of the widget has changed with text content, so 
        // re-adjust the alignment
        setPositionAlignment(mAlignment);
        
        // mark VBO for update
        if (mVertexAttrBufferObj != null) {
            mVertexAttrBufferObj.update(1);
        }  
        return this;
    }
    
    /**
     * Sets color of font outline. 
     * @param r
     * @param g
     * @param b
     * @return
     */
    public Text setOutlineColor(float r, float g, float b, float a) {
        mOutlineColor[0] = r;
        mOutlineColor[1] = g;
        mOutlineColor[2] = b;
        mOutlineColor[3] = a;
        return this;
    }
    
    /**
     * Registers with a {@link ColorWrapper} for the outline color. 
     * @param cp
     */
    public Text setOutlineColorPalette(ColorWrapper cp) {
        if (mOutlineColorPalette != null) {
            mOutlineColorPalette.removeListener(this);
        }
        mOutlineColorPalette = cp;
        cp.addListener(this, 0);
        return this;
    }

    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    public void extractLocations(String programName, int prog) {
        maPosition = glGetAttribLocation(prog, "aPosition");
        maTexCoord = glGetAttribLocation(prog, "aTexCoord");
        muMatrix = glGetUniformLocation(prog, "uMatrix");
        muTex = glGetUniformLocation(prog, "uTex");
        muColor = glGetUniformLocation(prog, "uColor");
        muOutlineColor = glGetUniformLocation(prog, "uOutlineColor");
    }
    
    @Override
    public void onColorChange(int colorIdx, ColorWrapper cp) {
        if (cp == mOutlineColorPalette) {
            final float[] c = cp.getActualColor();
            mOutlineColor[0] = c[0];
            mOutlineColor[1] = c[1];
            mOutlineColor[2] = c[2];
            mOutlineColor[3] = c[3];
        } else {
            super.onColorChange(colorIdx, cp);
        }
    }
    
    @Override
    public int getWidth() {
        return (int) (mVertexSpanX * getScaleX());
    }

    @Override
    public int getHeight() {
        return (int) (mVertexSpanY * getScaleY());
    }

    public void draw() {
        sGlState.apply(mProgram.getID());
        
        glBindBuffer(GL_ARRAY_BUFFER, mVertexAttrBufferObj.getID());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndexBufferObj.getID());

        glVertexAttribPointer(maPosition, 3, GL_FLOAT, false, BaseFont.STRIDE,
                0);
        glEnableVertexAttribArray(maPosition);
        glVertexAttribPointer(maTexCoord, 2, GL_FLOAT, false, BaseFont.STRIDE,
                12);
        glEnableVertexAttribArray(maTexCoord);

        glUniformMatrix4fv(muMatrix, 1, false, mMatrix, 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureObj.getID());
        glUniform1i(muTex, 0);

        glUniform4f(muColor, mColor[0], mColor[1], mColor[2], mColor[3]);
        glUniform4f(muOutlineColor, mOutlineColor[0], mOutlineColor[1], 
                mOutlineColor[2], mOutlineColor[3]);

        glDrawElements(GL_TRIANGLES, mIndexCnt, GL_UNSIGNED_SHORT, 0);
        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maTexCoord);
    }

    private void setupVertexAttrBufferObj() {
        mNumChar = mFont.fillVertexAttrBuffer(mText, mVertexAttrBuf);

        mVertexSpanX = mFont.getVertexBufferSpanX();
        mVertexSpanY = mFont.getVertexBufferSpanY();
        mVertexLeftX = mFont.getVertexBufferLeftX();
        mVertexTopY = mFont.getVertexBufferTopY();

        mVertexAttrBuf.rewind();
        mIndexCnt = BaseFont.getIndexBufferLen(mNumChar);
    }

    @Override
    public void getConfigurationSetup(String name,
            IBufferConfigurationSetup setup) {
        if (name.equals(mVertexAttrBufferObjDescr.getQName())) {
            setup.setBuffer(mVertexAttrBuf);
            setup.setTarget(Target.ARRAY);
            setup.setSize(BaseFont.getVertexAttrBufferLen(mNumChar) *
                    Float.SIZE / 8);
            return;
        }
        if (name.equals(mIndexBufferObjDescr.getQName())) {
            final int indexCnt = BaseFont.getIndexBufferLen(MAX_TEXT_LENGTH);
            ByteBuffer ebb = ByteBuffer.allocateDirect(indexCnt * Short.SIZE / 8);
            ebb.order(ByteOrder.nativeOrder());
            ShortBuffer sb = ebb.asShortBuffer();
            mFont.initIndexBuffer(sb);
            
            setup.setBuffer(sb.rewind());
            setup.setTarget(Target.ELEMENT);
            setup.enableAutoSize(true);
            return;
        }
        throw new IllegalArgumentException("Unknown descriptor name.");

    }

    @Override
    public void getUpdateSetup(String name, int pass, IBufferUpdateSetup setup) {
        // don't care about name because updates happen only on vertex buffer
        setup.setBuffer(mVertexAttrBuf);
        setup.setBufferPosition(0);
        setup.setOffset(0);
        setup.setSize(BaseFont.getVertexAttrBufferLen(mNumChar)*Float.SIZE/8);
        // text size, i.e. buffer size, may vary, so create new buffer 
        // instead of modifying sub data
        setup.enableBufferSubData(false);
    }

    public BaseWidget setPositionAlignment(PositionAlignment alignment) {
        mAlignment = alignment;
        // align widget based on mAlignment setting
        float offsetX = 0;
        float offsetY = 0;
        switch (alignment) {
        case LEFT_X_TOP_Y:
            offsetX = -mVertexLeftX;
            offsetY = -mVertexTopY;
            break;

        case RIGHT_X_TOP_Y:
            offsetX = -mVertexLeftX - mVertexSpanX;
            offsetY = -mVertexTopY;
            break;

        case LEFT_X_BOTTOM_Y:
            offsetX = -mVertexLeftX;
            offsetY = -mVertexTopY + mVertexSpanY;
            break;

        case RIGHT_X_BOTTOM_Y:
            offsetX = -mVertexLeftX - mVertexSpanX;
            offsetY = -mVertexTopY + mVertexSpanY;
            break;

        case CENTERED_XY:
            offsetX = -mVertexLeftX - mVertexSpanX / 2;
            offsetY = -mVertexTopY + mVertexSpanY / 2;
            break;

        case LEFT_X_CENTERED_Y:
            offsetX = -mVertexLeftX;
            offsetY = -mVertexTopY + mVertexSpanY / 2;
            break;

        case RIGHT_X_CENTERED_Y:
            offsetX = -mVertexLeftX - mVertexSpanX;
            offsetY = -mVertexTopY + mVertexSpanY / 2;
            break;

        }
        super.setLocalOffset((int) offsetX, (int) offsetY);
        return this;
    }

    @Override
    protected float getLocalLeft() {
        return mVertexLeftX;
    }

    @Override
    protected float getLocalRight() {
        return mVertexLeftX + mVertexSpanX;
    }

    @Override
    protected float getLocalTop() {
        return mVertexTopY;
    }

    @Override
    protected float getLocalBottom() {
        return mVertexTopY - mVertexSpanY;
    }

}
