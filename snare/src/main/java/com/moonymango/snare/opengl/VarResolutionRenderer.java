package com.moonymango.snare.opengl;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.opengl.BufferObj.IBufferConfigurationSetup;
import com.moonymango.snare.opengl.BufferObj.IBufferDataProvider;
import com.moonymango.snare.opengl.BufferObj.IBufferUpdateSetup;
import com.moonymango.snare.opengl.BufferObj.Target;
import com.moonymango.snare.opengl.GLObjDescriptor.GLObjType;
import com.moonymango.snare.opengl.ProgramObj.ILocationHolder;
import com.moonymango.snare.opengl.TextureObj.TextureSize;
import com.moonymango.snare.ui.PlayerGameView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform2f;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.moonymango.snare.opengl.GLES20Trace.glClear;
import static com.moonymango.snare.opengl.GLES20Trace.glClearColor;
import static com.moonymango.snare.opengl.GLES20Trace.glViewport;

/**
 * Lets the {@link PlayerGameView} render into a texture and then renders
 * the texture to screen. This allows rendering with reduced resolution.
 * <p>
 * Also implements {@link ILocationHolder} for the program which is used
 * to do final rendering to screen.
 */
public class VarResolutionRenderer extends BaseSnareClass implements IRenderer, ILocationHolder, IBufferDataProvider
{
    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;" +
                    "uniform vec2 uRes;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  float x = max(aPosition.x, 0.0) * uRes.s;" +
                    "  float y = max(aPosition.y, 0.0) * uRes.t;" +
                    "  vTexCoord = vec2(x, y);" +
                    "  gl_Position = aPosition;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision highp float;" +
                    "uniform sampler2D uTex;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTex, vTexCoord);" +
                    "}";

    private static final float[] VERTICES = {
            -1.0f, -1.0f, 0,
            1.0f, -1.0f, 0,
            -1.0f, 1.0f, 0,
            1.0f, 1.0f, 0
    };

    private static int saPosition;
    private static int suTex;
    private static int suRes;

    private final PlayerGameView mPlayerGameView;
    private final float mScale;
    private int mMaxResX;
    private int mMaxResY;
    private TextureSize mSize;
    private int mResX;
    private int mResY;

    private GLObjDescriptor mFBODescr;
    private FramebufferObj mFBO;
    private GLObjDescriptor mProgramDescr;
    private ProgramObj mProgram;
    private GLObjDescriptor mBufferDescr;
    private BufferObj mBufferObj;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private GLInfo mRenderInfo;
    private final GLState mGlState = new GLState();
    private float mFPS;
    private long mFrameCnt;
    private long mLastFPSUpdateTime;

    private boolean mUseNativeRes;

    /**
     * Create render. Max resolution is screen resolution.
     *
     * @param view
     */
    public VarResolutionRenderer(PlayerGameView view)
    {
        this(view, 0, 0, 0);
    }

    public VarResolutionRenderer(PlayerGameView view, float scale)
    {
        this(view, 0, 0, scale);
    }

    /**
     * Create renderer.
     *
     * @param view
     * @param maxResX Max. resolution x.
     * @param maxResY Max. resolution y.
     */
    public VarResolutionRenderer(PlayerGameView view, int maxResX, int maxResY)
    {
        this(view, maxResX, maxResY, 0);
    }

    private VarResolutionRenderer(PlayerGameView view, int maxResX, int maxResY, float scale)
    {
        super(view.mGame);

        mPlayerGameView = view;
        mScale = scale;
        mMaxResX = maxResX;
        mMaxResY = maxResY;
    }

    public int getMaxResolutionX()
    {
        return mMaxResX;
    }

    public int getMaxResolutionY()
    {
        return mMaxResY;
    }

    public IRenderer setResolution(int width, int height)
    {
        if (width < 1 || height < 1 || width > mMaxResX
                || height > mMaxResY) {
            throw new IllegalArgumentException("Invalid viewport size");
        }
        mResX = width;
        mResY = height;
        return this;
    }

    /**
     * Enable/disable directly rendering to surface framebuffer. When
     * enabled everything is rendered directly to screen (exactly like
     * {@link FullScreenRenderer}), i.e. using different resolution does
     * not work.
     *
     * @param enable
     */
    public void useNativeResolution(boolean enable)
    {
        mUseNativeRes = enable;
    }

    @Override
    public void extractLocations(String programName, int prog)
    {
        saPosition = glGetAttribLocation(prog, "aPosition");
        suTex = glGetUniformLocation(prog, "uTex");
        suRes = glGetUniformLocation(prog, "uRes");
    }

    @Override
    public void getConfigurationSetup(String name,
                                      IBufferConfigurationSetup setup)
    {
        setup.setBuffer(BufferObj.convert2FloatBuffer(VERTICES));
        setup.enableAutoSize(true);
        setup.setTarget(Target.ARRAY);
        setup.setUsage(GL_STATIC_DRAW);
    }

    // buffer object will never be updated, so the following update
    // callbacks will never be called
    @Override
    public void getUpdateSetup(String name, int pass,
                               IBufferUpdateSetup setup)
    {
    }



    public PlayerGameView[] getPlayerViews()
    {
        PlayerGameView[] v = {mPlayerGameView};
        return v;
    }

    public void onDrawFrame(GL10 gl)
    {
        final RenderOptions ro = mGame.getSettings().RENDER_OPTIONS;

        mGame.waitForDraw();

        // make sure everything is in GPU before actual drawing
        mGame.getGLObjCache().update();
        GLState.sync();
        drawPlayerView(ro);
        if (!mUseNativeRes) {
            drawFBOToScreen();
        }

        int e = glGetError();
        if (e != GL_NO_ERROR && ro.THROW_ON_GL_ERROR) {
            throw new IllegalStateException("GL error: 0x"
                    + Integer.toHexString(e));
        }

        long time = mGame.getLastMeasuredTime();

        // FPS counter
        mFrameCnt++;
        long delta = time - mLastFPSUpdateTime;
        if (delta > 1000) {
            float f = (float) 1000 / delta;
            mFPS = mFrameCnt / f;
            mFrameCnt = 0;
            mLastFPSUpdateTime = time;
        }

        mGame.notifyEndDraw();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        mGame.waitForDraw();

        mSurfaceHeight = height;
        mSurfaceWidth = width;
        if (mScale == 0) {
            mMaxResX = mMaxResX < 1 ? width : mMaxResX;
            mMaxResY = mMaxResY < 1 ? height : mMaxResY;
        } else {
            mMaxResX = (int) (width * mScale);
            mMaxResY = (int) (height * mScale);
        }
        mResX = mResX < 1 ? mMaxResX : mResX;
        mResY = mResY < 1 ? mMaxResY : mResY;

        mPlayerGameView.onSurfaceChanged(width, height);

        // choose color attachment texture size
        if (mSize == null) {
            mSize = TextureSize.fit(mMaxResX, mMaxResY);
            if (mSize == null) {
                throw new IllegalArgumentException("Invalid viewport dimension.");
            }
        }
        if (mRenderInfo.getMaxRenderbufferSize() < mSize.value()
                || mRenderInfo.getMaxTextureSize() < mSize.value()) {
            // TODO silently clamp values instead of throwing?
            throw new IllegalStateException("viewport size not supported");
        }

        initGL();
        mGame.notifyEndDraw();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        mGame.waitForDraw();

        //read EGL context properties + extensions
        if (mRenderInfo == null) {
            mRenderInfo = new GLInfo();
        }
        mRenderInfo.collectValues();
        mGame.getGLObjCache().reloadAll();
        mGame.notifyEndDraw();
    }

    public boolean hasSurface()
    {
        return mSurfaceWidth > 1 && mSurfaceHeight > 1;
    }

    public GLInfo getInfo()
    {
        return mRenderInfo;
    }

    public void onInit()
    {
    }

    public float getFPS()
    {
        return mFPS;
    }

    /**
     * Init GL stuff necessary to draw FBO to screen.
     */
    private void initGL()
    {
        if (mFBO != null) {
            return;
        }

        // prepare GL stuff (because renderer is bound to Game instance for
        // live time, do not care about releasing anything of it)
        mFBODescr = new GLObjDescriptor(mGame,
                VarResolutionRenderer.class.getName() + IGame.DELIMITER + mGame.getRandomString(),
                GLObjType.FRAMEBUFFER);
        mFBO = (FramebufferObj) mFBODescr.getHandle();
        if (!mFBO.isConfigured()) {
            mFBO.configure(mSize, TextureObjOptions.NEAREST_REPEAT);
        }

        mProgramDescr = new GLObjDescriptor(mGame,
                VarResolutionRenderer.class.getName() + IGame.DELIMITER + "program",
                GLObjType.PROGRAM);
        mProgram = (ProgramObj) mProgramDescr.getHandle();
        if (!mProgram.isConfigured()) {
            mProgram.configure(VERTEX_SHADER, FRAGMENT_SHADER, this);
        }

        mBufferDescr = new GLObjDescriptor(mGame,
                VarResolutionRenderer.class.getName() + IGame.DELIMITER + "vertex",
                GLObjType.BUFFER);
        mBufferObj = (BufferObj) mBufferDescr.getHandle();
        if (!mBufferObj.isConfigured()) {
            mBufferObj.configure(this);
        }
    }

    private void drawPlayerView(RenderOptions ro)
    {
        if (!mUseNativeRes) {
            glViewport(0, 0, mResX, mResY);
            glBindFramebuffer(GL_FRAMEBUFFER, mFBO.getID());
        }

        glClearColor(ro.BG_COLOR_R, ro.BG_COLOR_G, ro.BG_COLOR_B, 1.0f);
        if (mGame.getSettings().RENDER_OPTIONS.CLEAR_SCREEN) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
        mPlayerGameView.draw();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
    }

    private void drawFBOToScreen()
    {
        mGlState.apply(mProgram.getID());

        glBindBuffer(GL_ARRAY_BUFFER, mBufferObj.getID());
        glVertexAttribPointer(saPosition, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(saPosition);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mFBO.getColorAttachmentId());
        glUniform1i(suTex, 0);

        final float width = (float) mResX / mSize.value();
        final float height = (float) mResY / mSize.value();
        glUniform2f(suRes, width, height);

        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glDisableVertexAttribArray(saPosition);

    }
}
