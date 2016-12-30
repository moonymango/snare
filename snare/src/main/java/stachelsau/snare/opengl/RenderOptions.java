package stachelsau.snare.opengl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView.EGLConfigChooser;

public class RenderOptions implements EGLConfigChooser{

    public boolean THROW_ON_GL_ERROR = true;
    
    public float BG_COLOR_R;
    public float BG_COLOR_G;
    public float BG_COLOR_B;
    public boolean CLEAR_SCREEN = true;
    
    // EGL stuff
    public int EGL_RED_SIZE = 5;
    public int EGL_GREEN_SIZE = 6;
    public int EGL_BLUE_SIZE = 5;
    public int EGL_ALPHA_SIZE = 0;
    public int EGL_DEPTH_SIZE = 16;
    public int EGL_STENCIL_SIZE = 0;
    
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        // TODO prioC: maybe some day test frame rate cutting 
        // via EGL_MIN_SWAP_INTERVAL config attribute
        return null;
    }
    
    
    
}
