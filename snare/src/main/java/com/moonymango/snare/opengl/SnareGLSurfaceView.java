package com.moonymango.snare.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Surface view.
 */

public class SnareGLSurfaceView extends GLSurfaceView
{
    public SnareGLSurfaceView(Context context)
    {
        super(context);
    }

    @Override
    public boolean performClick()
    {
        // we have no onClickListeners here
        return false;
    }
}
