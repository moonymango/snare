package stachelsau.snare.demo.procedural;


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