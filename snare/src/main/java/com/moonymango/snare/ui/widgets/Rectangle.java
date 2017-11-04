package com.moonymango.snare.ui.widgets;

import com.moonymango.snare.game.IGame;
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
import com.moonymango.snare.res.texture.BaseTextureResource.TextureRegion;
import com.moonymango.snare.ui.PlayerGameView;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

public class Rectangle extends BaseTouchWidget implements IBufferDataProvider {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    private static final String VERTEX_SHADER_TEX =
            "precision mediump float;" +
            "uniform mat4 uMatrix;" +
            "uniform vec2 uTexOffs;" +
                    
            "attribute vec4 aPosition;" +
            "attribute vec2 aTexCoord;" +
    
            "varying vec2 vTexCoord;" +
     
            "void main(){" +
                "vTexCoord = aTexCoord + uTexOffs;" +
                "gl_Position = uMatrix  * aPosition;" +
            "}";
    
    private static final String VERTEX_SHADER_NO_TEX =
            "precision mediump float;" +
            "uniform mat4 uMatrix;" + 
            "attribute vec4 aPosition;" +
                 
            "void main(){" +
                "gl_Position = uMatrix  * aPosition;" +
            "}";
    
    private static final String FRAGMENT_SHADER_TEX =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "uniform sampler2D uTex;" +
            "varying vec2 vTexCoord;" +
            "void main(){" +
                "gl_FragColor = uColor * texture2D(uTex, vTexCoord);" +
            "}";
    
    private static final String FRAGMENT_SHADER_NO_TEX =
            "precision mediump float;" +
            "uniform vec4 uColor;" +
            "void main(){" +
                "gl_FragColor = uColor;" +
            "}"; 
    
    // need two different sets of locations because Rectangle between two
    // different programs
    /** Attribute location of vertex position. */
    private static int maPosition;
    private static int maPositionNoTex;
    /** Attribute location of texture coordinates. */
    private static int maTexCoord;
    /** Uniform location of texture */
    private static int muTex;
    private static int muTexOffs;
    /** Uniform location of view/projection matrix. */
    private static int muMatrix;
    private static int muMatrixNoTex;
    /** Uniform location of color. */
    private static int muColor;
    private static int muColorNoTex;
    
    private final GLObjDescriptor mProgramDescr;
    private final GLObjDescriptor mTextureObjDescr;
    private final TextureObjOptions mTextureOptions;
    private ProgramObj mProgram;
    private TextureObj mTextureObj;
    private final BaseTextureResource mTextureResource;
    
    private static final float LOCAL_LEFT = -1;
    private static final float LOCAL_RIGHT = 1;
    private static final float LOCAL_TOP = 1;
    private static final float LOCAL_BOTTOM = -1;
    static final float LOCAL_WIDTH = LOCAL_RIGHT - LOCAL_LEFT;
    static final float LOCAL_HEIGHT = LOCAL_TOP - LOCAL_BOTTOM;
            
    private int mWidth = 100;
    private int mHeight = 100;
    
    private final GLObjDescriptor mVertexAttrBufferObjDescr; 
    private BufferObj mVertexAttrBufferObj;
   
    private TextureRegion mRegion;
    private String mRegionName;
    private float[] mTexOffs = {0, 0};
    private final GLState mGlState = new GLState();    
    private final float[] mVertexAttribs = {
        //  x               y           z    s    t
        LOCAL_RIGHT,    LOCAL_BOTTOM,   0f,  0,   0,  // lower right
        LOCAL_RIGHT,    LOCAL_TOP,      0f,  0,   0,     // upper right  
        LOCAL_LEFT,     LOCAL_BOTTOM,   0f,  0,   0,  // lower left    
        LOCAL_LEFT,     LOCAL_TOP,      0f,  0,   0      // upper left
    };
    
    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    /**
     * Construct rectangle.
     * @param texture
     * @param options
     * @param regionName
     * @param alignment
     */
    public Rectangle(IGame game, BaseTextureResource texture, TextureObjOptions options,
            String regionName, PositionAlignment alignment, TouchSetting setting)
    {
        super(game, setting);
        mTextureResource = texture;
        mTextureOptions = options != null ? options : mGame.getSettings().mDefaultTextureOptions;
        
        if (mTextureResource != null) {
            // texture
            mTextureObjDescr = new GLObjDescriptor(game, mTextureResource.getName(), GLObjType.TEXTURE);
            mRegion = texture.getTextureRegion(regionName);
                
            // program
            mProgramDescr = new GLObjDescriptor(game, Rectangle.class.getName() + ".program_tex", GLObjType.PROGRAM);
            
            // when using textures, then each rectangle needs it's own
            // VBO due to texture regions, so chose random name
            final String s = Rectangle.class.getName() + ".vertices_" + mGame.getRandomString();
            mVertexAttrBufferObjDescr = new GLObjDescriptor(game, s, GLObjType.BUFFER);
            
        } else {
            mTextureObjDescr = null;
            // program
            mProgramDescr = new GLObjDescriptor(game, Rectangle.class.getName() + ".program_no_tex", GLObjType.PROGRAM);
            
            // VBO
            final String s = Rectangle.class.getName() + ".vertices_no_tex";
            mVertexAttrBufferObjDescr = new GLObjDescriptor(game, s, GLObjType.BUFFER);
        }
        
        setupVertexAttrBufferObj();
        setPositionAlignment(alignment != null ? alignment : 
                PositionAlignment.CENTERED_XY);
    }

    /**
     * Creates rectangle with default texture options.
     * @param texture
     */
    public Rectangle(IGame game, BaseTextureResource texture, TouchSetting setting)
    {
        this(game, texture, game.getSettings().mDefaultTextureOptions, null, PositionAlignment.CENTERED_XY, setting);
    }
    
    /**
     * Creates rectangle with specific texture options.
     * @param texture
     */
    public Rectangle(IGame game, BaseTextureResource texture, TextureObjOptions options, TouchSetting setting)
    {
        this(game, texture, options, null, PositionAlignment.CENTERED_XY, setting);
    }
    
    public Rectangle(IGame game, BaseTextureResource texture, TextureObjOptions options)
    {
        this(game, texture, options, null, PositionAlignment.CENTERED_XY, TouchSetting.NOT_TOUCHABLE);
    }
    
    /**
     * Creates {@link Rectangle} without texture.
     */
    public Rectangle(IGame game, TouchSetting setting)
    {
        this(game, null, null, null, PositionAlignment.CENTERED_XY, setting);
    }
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    
    public void extractLocations(String programName, int prog) {
        if (mTextureObjDescr != null) {
            maPosition = glGetAttribLocation(prog, "aPosition");
            maTexCoord = glGetAttribLocation(prog, "aTexCoord");
            muMatrix = glGetUniformLocation(prog, "uMatrix");
            muTex = glGetUniformLocation(prog, "uTex");
            muTexOffs = glGetUniformLocation(prog, "uTexOffs");
            muColor = glGetUniformLocation(prog, "uColor");
        } else {
            maPositionNoTex = glGetAttribLocation(prog, "aPosition");
            muMatrixNoTex = glGetUniformLocation(prog, "uMatrix");
            muColorNoTex = glGetUniformLocation(prog, "uColor");
        }
    }

    @Override
    public BaseWidget setBlendMode(BlendMode mode) {
        switch (mode) {
        case OPAQUE:
            // TODO prioA: disable blending
            break;
        case REPLACE:
            mGlState.enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            break;
        case TRANSPARENT:
            mGlState.enableBlend(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            break;
        }
        return super.setBlendMode(mode);
    }

    @Override
    public void onAttachToScreen(PlayerGameView view, int screenWidth,
                                 int screenHeight) {
        
        if (mTextureResource != null) {
            mTextureObj = (TextureObj) mTextureObjDescr.getHandle();
            if (!mTextureObj.isConfigured()) {
                mTextureObj.configure(mTextureResource, mTextureOptions);
            }    
            // program
            mProgram = (ProgramObj) mProgramDescr.getHandle();
            if (!mProgram.isConfigured()) {
                mProgram.configure(VERTEX_SHADER_TEX, FRAGMENT_SHADER_TEX, this); 
            }
            
        } else {
            mProgram = (ProgramObj) mProgramDescr.getHandle();
            if (!mProgram.isConfigured()) {
                mProgram.configure(VERTEX_SHADER_NO_TEX, FRAGMENT_SHADER_NO_TEX, 
                        this);                
            }
        }
        
        if (mVertexAttrBufferObj == null) {
            mVertexAttrBufferObj = (BufferObj) mVertexAttrBufferObjDescr.getHandle();
            if (!mVertexAttrBufferObj.isConfigured()) {
                mVertexAttrBufferObj.configure(this);
            }
        }
        
        super.onAttachToScreen(view, screenWidth, screenHeight);
    }

    @Override
    public void onDetachFromScreen() {
        // program
        mProgramDescr.releaseHandle(mProgram);
        mProgram = null;
        // texture
        if (mTextureObjDescr != null) {
            mTextureObjDescr.releaseHandle(mTextureObj);
            mTextureObj = null;
        }
        
        //VBO
        mVertexAttrBufferObjDescr.releaseHandle(mVertexAttrBufferObj);
        mVertexAttrBufferObj = null;
        
        super.onDetachFromScreen();
    }
    
    @Override
    public BaseWidget setScale(float x, float y) {
        if (x < 0 || y < 0)
            throw new IllegalArgumentException("Dimensions must not be less than 0.");
        
        mWidth = (int) (LOCAL_WIDTH * x);
        mHeight = (int) (LOCAL_HEIGHT * y);
        super.setScale(x, y);
        return this;
    }

    public int getWidth() {return mWidth;}
    public int getHeight() {return mHeight;}

    /**
     * Set texture coordinates via region name.
     * @param regionName
     * @return
     */
    public BaseWidget setTextureRegion(String regionName) {
        if (mTextureResource != null) { 
            mRegionName = regionName;
            mRegion = mTextureResource.getTextureRegion(regionName);
            setupVertexAttrBufferObj();
        }
        return this;
    }
    
    public String getTextureRegionName() {
        return mRegionName;
    }
    
    /**
     * Offset texture coordinates. This is an additional offset on top 
     * of region coordinates. 
     * @param x
     * @param y
     * @return This {@link Rectangle}.
     */
    public Rectangle setTextureOffset(float x, float y) {
        mTexOffs[0] = x;
        mTexOffs[1] = y;
        return this;
    }
    
    private void setupVertexAttrBufferObj() {
        
        // set texture region in vertex buffer in case we have a texture
        if (mTextureObjDescr != null) {
            // lower right
            mVertexAttribs[3] = mRegion.RIGHT;
            mVertexAttribs[4] = mRegion.BOTTOM;
            // upper right
            mVertexAttribs[8] = mRegion.RIGHT;
            mVertexAttribs[9] = mRegion.TOP;
            // lower left
            mVertexAttribs[13] = mRegion.LEFT;
            mVertexAttribs[14] = mRegion.BOTTOM;
            // upper left
            mVertexAttribs[18] = mRegion.LEFT;
            mVertexAttribs[19] = mRegion.TOP;
        }
        
        if (mVertexAttrBufferObj != null) {
            mVertexAttrBufferObj.update(1);
        } 
    }
    
    @Override
    public void getConfigurationSetup(String name,
            IBufferConfigurationSetup setup) {
        setup.setBuffer(BufferObj.convert2FloatBuffer(mVertexAttribs));
        setup.enableAutoSize(true);
        setup.setTarget(Target.ARRAY);      
    }

    @Override
    public void getUpdateSetup(String name, int pass, IBufferUpdateSetup setup) {
        setup.setBuffer(BufferObj.convert2FloatBuffer(mVertexAttribs));
        setup.setBufferPosition(0);
        setup.setOffset(0);
        setup.setSize(mVertexAttribs.length * Float.SIZE/8);
        setup.enableBufferSubData(true);
    }
    
    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------
    public void draw() {
        mGlState.apply(mProgram.getID());
        
        if (mTextureObjDescr != null) {
            glUniformMatrix4fv(muMatrix, 1, false, mMatrix, 0);
            glUniform4f(muColor, mColor[0], mColor[1], mColor[2], mColor[3]);
            glUniform2f(muTexOffs, mTexOffs[0], mTexOffs[1]);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, mTextureObj.getID());
            glUniform1i(muTex, 0);
            
            glBindBuffer(GL_ARRAY_BUFFER, mVertexAttrBufferObj.getID());
            glVertexAttribPointer(maPosition, 3, GL_FLOAT, false, 20, 0);
            glEnableVertexAttribArray(maPosition);
            glVertexAttribPointer(maTexCoord, 3, GL_FLOAT, false, 20, 12);
            glEnableVertexAttribArray(maTexCoord);
        } else {
            glUniformMatrix4fv(muMatrixNoTex, 1, false, mMatrix, 0);
            glUniform4f(muColorNoTex, mColor[0], mColor[1], mColor[2], mColor[3]);
            
            glBindBuffer(GL_ARRAY_BUFFER, mVertexAttrBufferObj.getID());
            glVertexAttribPointer(maPositionNoTex, 3, GL_FLOAT, false, 20, 0);
            glEnableVertexAttribArray(maPositionNoTex);
        }
       
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4); 
        glDisableVertexAttribArray(maTexCoord);
        glDisableVertexAttribArray(maPosition);
        glDisableVertexAttribArray(maPositionNoTex);
    }

    @Override
    public BaseWidget setPositionAlignment(PositionAlignment alignment) {
     // align widget based on mAlignment setting
        float offsetX = 0;
        float offsetY = 0;
        switch (alignment) {
        case LEFT_X_TOP_Y:
            offsetX = -LOCAL_LEFT;
            offsetY = -LOCAL_TOP;
            break;
            
        case RIGHT_X_TOP_Y:
            offsetX = -LOCAL_LEFT - LOCAL_WIDTH;
            offsetY = -LOCAL_TOP;
            break;
            
        case LEFT_X_BOTTOM_Y:
            offsetX = -LOCAL_LEFT;
            offsetY = -LOCAL_TOP + LOCAL_HEIGHT;
            break;
            
        case RIGHT_X_BOTTOM_Y:
            offsetX = -LOCAL_LEFT - LOCAL_WIDTH;
            offsetY = -LOCAL_TOP + LOCAL_HEIGHT;
            break;
            
        case CENTERED_XY:
            offsetX = -LOCAL_LEFT - LOCAL_WIDTH / 2 ;
            offsetY = -LOCAL_TOP + LOCAL_HEIGHT / 2;        
            break;
        
        case LEFT_X_CENTERED_Y:
            offsetX = -LOCAL_LEFT;
            offsetY = -LOCAL_TOP + LOCAL_HEIGHT / 2;
            break;
            
        case RIGHT_X_CENTERED_Y:
            offsetX = -LOCAL_LEFT - LOCAL_WIDTH;
            offsetY = -LOCAL_TOP + LOCAL_HEIGHT / 2;
            break;
        
        }
        super.setLocalOffset((int) offsetX, (int) offsetY);
        return this;
    } 
    
    
    @Override
    protected float getLocalLeft() {return LOCAL_LEFT;}

    @Override
    protected float getLocalRight() {return LOCAL_RIGHT;}

    @Override
    protected float getLocalTop() {return LOCAL_TOP;}

    @Override
    protected float getLocalBottom() {return LOCAL_BOTTOM;}


    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------

}
