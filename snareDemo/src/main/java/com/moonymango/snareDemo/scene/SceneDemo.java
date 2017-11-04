package com.moonymango.snareDemo.scene;

import com.moonymango.snare.game.BaseGameActivity;
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.opengl.FullScreenRenderer;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.res.xml.BMFont;
import com.moonymango.snare.res.xml.BMFontXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snareDemo.Asset;


public class SceneDemo extends BaseGameActivity
{

    @Override
    public String getName()
    {
        return SceneDemo.class.getName();
    }

    @Override
    public GameSettings onLoadGameSettings(IGame game)
    {
        GameSettings s = new GameSettings(game);
        s.RENDER_OPTIONS.BG_COLOR_B = 1f;
        s.RENDER_OPTIONS.BG_COLOR_R = 0.5f;
        s.RENDER_OPTIONS.BG_COLOR_G = 0.5f;
        s.PRINT_STATS = true;

        // input events
        s.INPUT_EVENT_MASK.SCROLL_ENABLED = true;
        s.INPUT_EVENT_MASK.FLING_ENABLED = true;
        s.INPUT_EVENT_MASK.SCALE_ENABLED = true;
        s.INPUT_EVENT_MASK.DOUBLE_TAP_ENABLED = true;

        return s;
    }

    @Override
    public IGameState onLoadInitialGameState(IGame game)
    {
        return new GameState(game);
    }

    @Override
    public BaseFont onLoadSystemFont(IGame game)
    {
        XMLResource<BMFont> fontRes = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler(game));
        XMLResHandle<BMFont> fontHnd = fontRes.getHandle();
        return fontHnd.getContent();
    }

    @Override
    public IRenderer onLoadRenderer(IGame game, PlayerGameView view)
    {
        return new FullScreenRenderer(view);
    }

}
