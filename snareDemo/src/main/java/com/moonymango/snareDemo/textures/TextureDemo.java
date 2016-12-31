package com.moonymango.snareDemo.textures;

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


public class TextureDemo extends BaseGameActivity {
    
    public String getName() {
        return TextureDemo.class.getName();
    }
    
    @Override
    public GameSettings onLoadGameSettings() {
        GameSettings s = new GameSettings();
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
        return new FullScreenRenderer(view);
    }
   
}
