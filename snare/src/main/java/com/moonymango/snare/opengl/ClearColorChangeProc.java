package com.moonymango.snare.opengl;

import com.moonymango.snare.game.Game;
import com.moonymango.snare.proc.ProcessManager.BaseProcess;
import com.moonymango.snare.ui.ColorWrapper;
import com.moonymango.snare.ui.ColorWrapper.IColorSeqListener;

/**
 * Changes the GL clear color. The color is delivered by a
 * {@link ColorWrapper}.
 */
public class ClearColorChangeProc extends BaseProcess implements IColorSeqListener
{
    private final ColorWrapper mCP;
    private final RenderOptions mRo = Game.get().getSettings().RENDER_OPTIONS;
    
    public ClearColorChangeProc(ColorWrapper cp) {
        mCP = cp;
    }
  
    @Override
    public void onColorChange(int colorIdx, ColorWrapper cp) {
        final float[] c = cp.getActualColor();
        mRo.BG_COLOR_R = c[0];
        mRo.BG_COLOR_G = c[1];
        mRo.BG_COLOR_B = c[2];
    }

    @Override
    protected void onInit() {
        mCP.addListener(this, 0);
    }

    @Override
    protected boolean onUpdate(long realTime, float realDelta,
            float virtualDelta) {
        
        return true;
    }

    @Override
    protected void onKill() {
        mCP.removeListener(this);
    }

}
