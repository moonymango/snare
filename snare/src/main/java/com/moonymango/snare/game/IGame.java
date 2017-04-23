package com.moonymango.snare.game;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;

import com.moonymango.snare.audio.SnareAudioManager;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.opengl.GLObjCache;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.proc.ProcessManager;
import com.moonymango.snare.res.ResourceCache;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;

/**
 * Interface of main game class.
 */

public interface IGame
{
    String ENGINE_NAME = "snare";
    String DELIMITER = ".";

    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    void waitForDraw();

    void notifyEndDraw();

    void onResume();

    void onPause();

    String getName();

    EventManager getEventManager();

    ResourceCache getResourceCache();

    SnareAudioManager getAudioManager();

    IRenderer getRenderer();

    PlayerGameView getPrimaryView();

    GameSettings getSettings();

    BaseFont getSystemFont();

    void setScratchPadData(Object data);

    Object getScratchPadData();

    String getRandomString();

    int getRandomInt();

    int getRandomInt(int min, int max);

    /**
     * @return Random float in [0..1];
     */
    float getRandomFloat();

    /**
     * @return Random float in [min..max];
     */
    float getRandomFloat(float min, float max);

    /**
     * Returns a resource string like {@link Activity}.getString()
     * @param resId
     * @return
     */
    String getResourceString(int resId);

    /**
     * @return retrieves {@link SharedPreferences} from the context.
     */
    SharedPreferences getPreferences(String name);

    Application getApplication();

    GLSurfaceView getSurfaceView();

    ProcessManager getProcManager();

    GLObjCache getGLObjCache();

    /** Returns active game state. */
    IGameState getGameState();

    /** Returns previous game state. */
    IGameState getPrevGameState();

    /** Returns state before previous game state. */
    IGameState getPrevPrevGameState();

    IGameState getOnPauseGameState();

    IPhysics getPhysics();

    void stopVirtualTime();

    void startVirtualTime();

    /** Returns last time taken from the system. */
    long getLastMeasuredTime();

    /** Returns game internal realtime. */
    long getRealTime();

    void setVirtualTimeFactor(float factor);

    float getVirtualTimeFactor();

    void vibrate(long time);

    void showToast(String msg);

    GameObj addGameObj(GameObj obj);

    void removeGameObj(GameObj obj);

    GameObj getObjById(int id);

    GameObj getObjByListIdx(int idx);

    /**
     * Gets game object by their list index. In case the object is not covered
     * by specified layer, null is returned.
     * @param idx
     * @param layer
     * @return
     */
    GameObj getObjByListIdx(int idx, GameObj.GameObjLayer layer);

    /**
     * Gets total number of game objects.
     * @return
     */
    int getGameObjCnt();

    BaseGameView addGameView(BaseGameView view);

    void removeGameView(BaseGameView view);

    /**
     * Sets game state which will become active in case of a call to
     * the activity's onPause(). In this case the game state logic
     * is called like this:
     *   - onActivate() is called immediately from onPause()
     *   - after the app resumes, onUpdate() is called regularly (so this
     *     may be used to cover onResume())
     * @param state State or null in case that no state change should happen
     *               on pause
     */
    void setOnPauseGameState(IGameState state);

    Activity getActivity();

    void showMessage(String msg, String buttonText, DialogInterface.OnClickListener listener);

    public enum ClockType {
        REALTIME,
        VIRTUAL
    }
}
