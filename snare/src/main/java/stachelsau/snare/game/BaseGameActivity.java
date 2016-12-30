package stachelsau.snare.game;

import stachelsau.snare.events.DefaultEventPool;
import stachelsau.snare.events.EventManager;
import stachelsau.snare.opengl.IRenderer;
import stachelsau.snare.physics.IPhysics;
import stachelsau.snare.physics.SimplePhysics;
import stachelsau.snare.res.ResourceCache;
import stachelsau.snare.ui.BaseFont;
import stachelsau.snare.ui.PlayerGameView;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public abstract class BaseGameActivity extends Activity {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------

    // ---------------------------------------------------------
    // fields
    // --------------------------------------------------------- 
       
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
        final Game game = Game.get(this);
        applySettings(game.getSettings());
        setContentView(game.prepareGLSurfaceView());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Game.get().getSurfaceView().onPause();
        Game.get().onPause();
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        Game.get().getSurfaceView().onResume();
        Game.get().onResume(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    
    public GameSettings onLoadGameSettings() {
        return new GameSettings();
    }
    
    public PlayerGameView onLoadPrimaryPlayerView() {
        return new PlayerGameView();
    }
    
    public IPhysics onLoadPhysics() {
        return new SimplePhysics();
    }
    
    public EventManager onLoadEventManager() {
        return new EventManager(new DefaultEventPool());
    };
    
    public abstract IRenderer onLoadRenderer(PlayerGameView view);
    public abstract String getName();
    public abstract IGameState onLoadInitialGameState();
    
    /**
     * Loads system font. When this is called, the {@link ResourceCache} is
     * already operational and can be used.
     * @return Font or null in case system output should be suppressed.
     */
    public abstract BaseFont onLoadSystemFont();
    
    
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
