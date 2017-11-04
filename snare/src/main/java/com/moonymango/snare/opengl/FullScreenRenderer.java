package com.moonymango.snare.opengl;

import com.moonymango.snare.game.BaseSnareClass;
import com.moonymango.snare.ui.PlayerIOGameView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glGetError;
import static com.moonymango.snare.opengl.GLES20Trace.glClear;
import static com.moonymango.snare.opengl.GLES20Trace.glClearColor;
import static com.moonymango.snare.opengl.GLES20Trace.glViewport;


public class FullScreenRenderer extends BaseSnareClass implements IRenderer
{

    private final PlayerIOGameView mPlayerGameView;
    private GLInfo mRenderInfo;
    private float mFPS;
    private long mFrameCnt;
    private long mLastFPSUpdateTime;
    private int mSurfaceWidth;
    private int mSurfaceHeight;


    public FullScreenRenderer(PlayerIOGameView view)
    {
        super(view.mGame);

        if (view == null) {
            throw new IllegalArgumentException("Missing player view");
        }
        mPlayerGameView = view;
    }


    public PlayerIOGameView[] getPlayerViews()
    {
        PlayerIOGameView[] v = {mPlayerGameView};
        return v;
    }

    public void onDrawFrame(GL10 gl)
    {
        final RenderOptions ro = mGame.getSettings().RENDER_OPTIONS;
        mGame.waitForDraw();

        glClearColor(ro.BG_COLOR_R, ro.BG_COLOR_G, ro.BG_COLOR_B, 1.0f);
        if (mGame.getSettings().RENDER_OPTIONS.CLEAR_SCREEN) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        // make sure everything is in GPU before actual drawing
        mGame.getGLObjCache().update();
        GLState.sync();
        mPlayerGameView.draw();

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

        glViewport(0, 0, width, height);
        mPlayerGameView.onSurfaceChanged(width, height);

        mSurfaceHeight = height;
        mSurfaceWidth = width;

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

}
