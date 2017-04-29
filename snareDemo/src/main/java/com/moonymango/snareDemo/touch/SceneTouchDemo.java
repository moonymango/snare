package com.moonymango.snareDemo.touch;

import com.moonymango.snare.events.EventManager;
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

public class SceneTouchDemo extends BaseGameActivity {
    
    @Override
    public String getName() {
        return SceneTouchDemo.class.getName();
    }
    
    @Override
    public EventManager onLoadEventManager(IGame game) {
        return new EventManager(new DemoEventPool(game));
    }

    @Override
    public GameSettings onLoadGameSettings(IGame game) {
        GameSettings s = new GameSettings(game);
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
    public IGameState onLoadInitialGameState(IGame game) {
        return new GameState(game);
    }

    @Override
    public BaseFont onLoadSystemFont(IGame game) {
        XMLResource<BMFont> fontRes = new XMLResource<BMFont>(Asset.COURIER, new BMFontXMLHandler(game));
        XMLResHandle<BMFont> fontHnd = fontRes.getHandle();
        return fontHnd.getContent();
    }
    
    
}
