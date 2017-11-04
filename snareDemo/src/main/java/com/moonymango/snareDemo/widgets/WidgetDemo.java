package com.moonymango.snareDemo.widgets;


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

public class WidgetDemo extends BaseGameActivity
{

    @Override
    public String getName()
    {
        return WidgetDemo.class.getName();
    }

    @Override
    public GameSettings onLoadGameSettings(IGame game)
    {
        GameSettings s = new GameSettings(game);
        s.PRINT_STATS = true;
        s.INPUT_EVENT_MASK.DOWN_ENABLED = true;
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
        XMLResource<BMFont> xmlRes = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler(game));
        XMLResHandle<BMFont> xmlHnd = xmlRes.getHandle();
        return xmlHnd.getContent();
    }

    @Override
    public IRenderer onLoadRenderer(IGame game, PlayerGameView view)
    {
        return new FullScreenRenderer(view);
    }

}