package stachelsau.snare.demo.physics;

import stachelsau.snare.demo.Asset;
import stachelsau.snare.game.BaseGameActivity;
import stachelsau.snare.game.GameSettings;
import stachelsau.snare.game.IGameState;
import stachelsau.snare.opengl.FullScreenRenderer;
import stachelsau.snare.opengl.IRenderer;
import stachelsau.snare.res.xml.BMFont;
import stachelsau.snare.res.xml.BMFontXMLHandler;
import stachelsau.snare.res.xml.XMLResHandle;
import stachelsau.snare.res.xml.XMLResource;
import stachelsau.snare.ui.BaseFont;
import stachelsau.snare.ui.PlayerGameView;

public class SimplePhysicsDemo extends BaseGameActivity {
    
    @Override
    public String getName() {
        return SimplePhysicsDemo.class.getName();
    }

    @Override
    public GameSettings onLoadGameSettings() {
        GameSettings s = new GameSettings();
        s.RENDER_OPTIONS.BG_COLOR_B = 0.2f;
        s.RENDER_OPTIONS.BG_COLOR_G = 0.1f;
        s.RENDER_OPTIONS.BG_COLOR_R = 0.1f;
        //s.PRINT_STATS = true;
        
        // generate scroll events
        s.INPUT_EVENT_MASK.SCROLL_ENABLED = true;
       
        return s;
    }

    @Override
    public IRenderer onLoadRenderer(PlayerGameView view) {
        return new FullScreenRenderer(view);
    }

    @Override
    public IGameState onLoadInitialGameState() {
        return new GameState();
    }

    @Override
    public BaseFont onLoadSystemFont() {
        XMLResource<BMFont> fontRes = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler());
        XMLResHandle<BMFont> fontHnd = fontRes.getHandle();
        return fontHnd.getContent();
    }

    
}
