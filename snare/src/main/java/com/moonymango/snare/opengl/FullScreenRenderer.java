package com.moonymango.snare.opengl;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_NO_ERROR;
import static android.opengl.GLES20.glGetError;
import static com.moonymango.snare.opengl.GLES20Trace.glClear;
import static com.moonymango.snare.opengl.GLES20Trace.glClearColor;
import static com.moonymango.snare.opengl.GLES20Trace.glViewport;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.ui.PlayerGameView;


public class FullScreenRenderer implements IRenderer {
    
    private final PlayerGameView mPlayerGameView;
    private GLInfo mRenderInfo;
    private float mFPS; 
    private long mFrameCnt;
    private long mLastFPSUpdateTime;
    private int mSurfaceWidth;
    private int mSurfaceHeight;
    
           
    public FullScreenRenderer(PlayerGameView view) {
        if (view == null) {
            throw new IllegalArgumentException("Missing player view");
        }
        mPlayerGameView = view;
    }
        
    public void print(String s) {
        mPlayerGameView.debugPrint(s);
    }

    public void clear() {
        mPlayerGameView.debugPrintClear();
    }

    public int getPlayerViewCnt() {
        return 1;
    }

    public PlayerGameView getPlayerViewByIdx(int idx) {
        return mPlayerGameView;
    }

    public void onDrawFrame(GL10 gl) {
        final Game game = Game.get();
        final RenderOptions ro = game.getSettings().RENDER_OPTIONS;
        game.waitForDraw();
        
        glClearColor(ro.BG_COLOR_R, ro.BG_COLOR_G, ro.BG_COLOR_B, 1.0f);
        if (game.getSettings().RENDER_OPTIONS.CLEAR_SCREEN) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
        
        // make sure everything is in GPU before actual drawing
        game.getGLObjCache().update();
        GLState.sync(); // FIXME do this in onSurfaceCreated?
        mPlayerGameView.draw();
        
        int e = glGetError();
        if (e != GL_NO_ERROR && ro.THROW_ON_GL_ERROR) {
            throw new IllegalStateException("GL error: 0x" 
                    + Integer.toHexString(e));
        }
        
        long time = Game.get().getLastMeasuredTime();
        
        // FPS counter
        mFrameCnt++;
        long delta = time - mLastFPSUpdateTime;
        if (delta > 1000) {
            float f = (float) 1000/delta;
            mFPS = mFrameCnt/f;
            mFrameCnt = 0;
            mLastFPSUpdateTime = time;
        }
        
        game.notifyEndDraw();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        final Game game = Game.get();
        game.waitForDraw();
        
        glViewport(0, 0, width, height);
        mPlayerGameView.onSurfaceChanged(width, height);
        
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        
        game.notifyEndDraw();
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        final Game game = Game.get();
        game.waitForDraw();
        
        //read EGL context properties + extensions
        if (mRenderInfo ==null) {
            mRenderInfo = new GLInfo();
        }
        mRenderInfo.collectValues();
        game.getGLObjCache().reloadAll();
        
        game.notifyEndDraw();
    }
    
    public boolean hasSurface() {
        return mSurfaceWidth > 1 && mSurfaceHeight > 1;
    }

    public GLInfo getInfo() {
        return mRenderInfo;
    }

    public void onInit() {}

    public float getFPS() {
        return mFPS;
    }
    
}
