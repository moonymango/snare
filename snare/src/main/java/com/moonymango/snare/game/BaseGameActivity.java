package com.moonymango.snare.game;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.moonymango.snare.events.DefaultEventPool;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.physics.SimplePhysics;
import com.moonymango.snare.res.ResourceCache;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;
import com.moonymango.snare.ui.PlayerIOGameView;

public abstract class BaseGameActivity extends Activity {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // --------------------------------------------------------- 
    protected IGame mGame;

    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SnareGame g = new SnareGame(this);
        applySettings(g.getSettings());
        setContentView(g.prepareGLSurfaceView());
        mGame = g;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGame.getSurfaceView().onPause();
        mGame.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGame.onResume();
        mGame.getSurfaceView().onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    public GameSettings onLoadGameSettings(IGame game)
    {
        return new GameSettings(game);
    }
    
    public PlayerGameView onLoadPrimaryPlayerView(IGame game)
    {
        return new PlayerIOGameView(game);
    }
    
    public IPhysics onLoadPhysics(IGame game)
    {
        return new SimplePhysics(game);
    }
    
    public EventManager onLoadEventManager(IGame game)
    {
        return new EventManager(new DefaultEventPool(game));
    }

    public abstract IRenderer onLoadRenderer(IGame game, PlayerGameView view);
    public abstract String getName();
    public abstract IGameState onLoadInitialGameState(IGame game);
    
    /**
     * Loads system font. When this is called, the {@link ResourceCache} is
     * already operational and can be used.
     * @return Font or null in case system output should be suppressed.
     */
    public abstract BaseFont onLoadSystemFont(IGame game);
    
    
    protected void applySettings(GameSettings s) {
        setRequestedOrientation(s.SCREEN_ORIENTATION);
        
        if (s.NO_TITLE) {
          requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        
        int flags = 0;
        int mask = 0;
        
        flags |= (s.FULL_SCREEN) ? WindowManager.LayoutParams.FLAG_FULLSCREEN : 0;
        mask |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        
        flags |= (s.KEEP_SCREEN_ON) ? WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON : 0;
        mask |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        

        getWindow().setFlags(flags, mask);
    }
    
    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------

}
