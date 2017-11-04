package com.moonymango.snare.opengl;

import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.PlayerIOGameView;

import android.opengl.GLSurfaceView;

public interface IRenderer extends GLSurfaceView.Renderer {
    
    void onInit();

    /**
     * Returns an array containing all views which are drawn by this renderer.
     * @return
     */
    PlayerGameView[] getPlayerViews();
    
    /**
     * Returns data of OpenGL implementation. 
     * @return
     */
    GLInfo getInfo();
    
    /**
     * Indicates whether or not the renderer has an EGL context.
     * @return
     */
    boolean hasSurface();
    
    /**
     * Returns frames per second.
     * @return
     */
    float getFPS();
    
}
