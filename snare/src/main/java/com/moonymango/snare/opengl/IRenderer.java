package com.moonymango.snare.opengl;

import com.moonymango.snare.ui.PlayerGameView;
import android.opengl.GLSurfaceView;

public interface IRenderer extends GLSurfaceView.Renderer {
    
    void onInit();
    
    /**
     * Prints to debug console.
     * @param s
     */
    void print(String s);
    
    /**
     * Clears the debug console.
     */
    void clear();
    
    /**
     * Returns the number of player views that are drawn by this renderer.
     * @return 1 for normal renderer, 2+ for a split screen
     */
    int getPlayerViewCnt();
    
    /**
     * Returns the player view.
     * @param idx
     * @return
     */
    PlayerGameView getPlayerViewByIdx(int idx);
    
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
