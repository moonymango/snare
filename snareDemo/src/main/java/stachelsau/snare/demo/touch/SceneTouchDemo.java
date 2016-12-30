package stachelsau.snare.demo.touch;

import stachelsau.snare.demo.Asset;
import stachelsau.snare.events.EventManager;
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

public class SceneTouchDemo extends BaseGameActivity {
    
    @Override
    public String getName() {
        return SceneTouchDemo.class.getName();
    }
    
    @Override
    public EventManager onLoadEventManager() {
        return new EventManager(new DemoEventPool());
    }

    @Override
    public GameSettings onLoadGameSettings() {
        GameSettings s = new GameSettings();
        s.INPUT_EVENT_MASK.DOWN_ENABLED = true;
        s.RENDER_OPTIONS.BG_COLOR_B = 0.2f;
        s.PRINT_STATS = true;
        s.SOUND_MAX_STREAMS = 8; 
        //s.mDummyLoops = 200000;
        
        //s.MULTI_THREADED = true;
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
