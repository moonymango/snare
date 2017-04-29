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

    float getRandomFloat();

    float getRandomFloat(float min, float max);

    String getResourceString(int resId);

    SharedPreferences getPreferences(String name);

    Application getApplication();

    GLSurfaceView getSurfaceView();

    ProcessManager getProcManager();

    GLObjCache getGLObjCache();

    IGameState getGameState();

    IGameState getPrevGameState();

    IGameState getPrevPrevGameState();

    IGameState getOnPauseGameState();

    IPhysics getPhysics();

    void stopVirtualTime();

    void startVirtualTime();

    long getLastMeasuredTime();

    long getRealTime();

    void setVirtualTimeFactor(float factor);

    float getVirtualTimeFactor();

    void vibrate(long time);

    void showToast(String msg);

    GameObj addGameObj(GameObj obj);

    void removeGameObj(GameObj obj);

    GameObj getObjById(int id);

    GameObj getObjByListIdx(int idx);

    GameObj getObjByListIdx(int idx, GameObj.GameObjLayer layer);

    int getGameObjCnt();

    BaseGameView addGameView(BaseGameView view);

    void removeGameView(BaseGameView view);

    void setOnPauseGameState(IGameState state);

    Activity getActivity();

    void showMessage(String msg, String buttonText, DialogInterface.OnClickListener listener);

    public enum ClockType {
        REALTIME,
        VIRTUAL
    }
}
