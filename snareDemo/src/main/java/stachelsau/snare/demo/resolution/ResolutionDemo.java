package stachelsau.snare.demo.resolution;

import stachelsau.snare.demo.Asset;
import stachelsau.snare.game.BaseGameActivity;
import stachelsau.snare.game.GameSettings;
import stachelsau.snare.game.IGameState;
import stachelsau.snare.opengl.IRenderer;
import stachelsau.snare.opengl.VarResolutionRenderer;
import stachelsau.snare.res.xml.BMFont;
import stachelsau.snare.res.xml.BMFontXMLHandler;
import stachelsau.snare.res.xml.XMLResHandle;
import stachelsau.snare.res.xml.XMLResource;
import stachelsau.snare.ui.BaseFont;
import stachelsau.snare.ui.PlayerGameView;


public class ResolutionDemo extends BaseGameActivity {
    
    @Override
    public String getName() {
        return ResolutionDemo.class.getName();
    }
    
    @Override
    public GameSettings onLoadGameSettings() {
        GameSettings s = new GameSettings();
        s.RENDER_OPTIONS.BG_COLOR_B = 0.3f;
        s.PRINT_STATS = true;
        
        // input events
        s.INPUT_EVENT_MASK.SCROLL_ENABLED = true;
        s.INPUT_EVENT_MASK.FLING_ENABLED = true;
        s.INPUT_EVENT_MASK.SCALE_ENABLED = true;
        s.INPUT_EVENT_MASK.DOUBLE_TAP_ENABLED = true;
        
        return s;
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

    @Override
    public IRenderer onLoadRenderer(PlayerGameView view) {
        return new VarResolutionRenderer(view, 0.5f);
    }
   
}
