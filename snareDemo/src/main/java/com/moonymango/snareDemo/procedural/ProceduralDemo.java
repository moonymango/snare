package com.moonymango.snareDemo.procedural;


import com.moonymango.snareDemo.Asset;
import com.moonymango.snare.game.BaseGameActivity;
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.opengl.FullScreenRenderer;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.res.xml.BMFont;
import com.moonymango.snare.res.xml.BMFontXMLHandler;
import com.moonymango.snare.res.xml.XMLResHandle;
import com.moonymango.snare.res.xml.XMLResource;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;

public class ProceduralDemo extends BaseGameActivity {
    
    @Override
    public String getName() {
        return ProceduralDemo.class.getName();
    }
    
    @Override
    public GameSettings onLoadGameSettings() {
        GameSettings s = new GameSettings();
        s.PRINT_STATS = true;
        s.INPUT_EVENT_MASK.DOWN_ENABLED = true;
        return s;
    }

    @Override
    public IGameState onLoadInitialGameState() {
        return new GameState();
    }

    @Override
    public BaseFont onLoadSystemFont() {
        XMLResource<BMFont> xmlRes = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler());
        XMLResHandle<BMFont> xmlHnd = xmlRes.getHandle();
        return xmlHnd.getContent();
    }

    @Override
    public IRenderer onLoadRenderer(PlayerGameView view) {
        return new FullScreenRenderer(view);
    }

}