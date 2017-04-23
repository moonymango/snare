package com.moonymango.snareTest;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;

import com.moonymango.snare.audio.SnareAudioManager;
import com.moonymango.snare.events.EventManager;
import com.moonymango.snare.game.BaseGameView;
import com.moonymango.snare.game.GameObj;
import com.moonymango.snare.game.GameSettings;
import com.moonymango.snare.game.IGame;
import com.moonymango.snare.game.IGameState;
import com.moonymango.snare.opengl.GLObjCache;
import com.moonymango.snare.opengl.IRenderer;
import com.moonymango.snare.physics.IPhysics;
import com.moonymango.snare.proc.ProcessManager;
import com.moonymango.snare.res.ResourceCache;
import com.moonymango.snare.ui.BaseFont;
import com.moonymango.snare.ui.PlayerGameView;

/**
 * Created by moonymango on 23.04.17.
 */

public class GameStub implements IGame
{
    @Override
    public void waitForDraw()
    {

    }

    @Override
    public void notifyEndDraw()
    {

    }

    @Override
    public void onResume()
    {

    }

    @Override
    public void onPause()
    {

    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public EventManager getEventManager()
    {
        return null;
    }

    @Override
    public ResourceCache getResourceCache()
    {
        return null;
    }

    @Override
    public SnareAudioManager getAudioManager()
    {
        return null;
    }

    @Override
    public IRenderer getRenderer()
    {
        return null;
    }

    @Override
    public PlayerGameView getPrimaryView()
    {
        return null;
    }

    @Override
    public GameSettings getSettings()
    {
        return null;
    }

    @Override
    public BaseFont getSystemFont()
    {
        return null;
    }

    @Override
    public void setScratchPadData(Object data)
    {

    }

    @Override
    public Object getScratchPadData()
    {
        return null;
    }

    @Override
    public String getRandomString()
    {
        return null;
    }

    @Override
    public int getRandomInt()
    {
        return 0;
    }

    @Override
    public int getRandomInt(int min, int max)
    {
        return 0;
    }

    @Override
    public float getRandomFloat()
    {
        return 0;
    }

    @Override
    public float getRandomFloat(float min, float max)
    {
        return 0;
    }

    @Override
    public String getResourceString(int resId)
    {
        return null;
    }

    @Override
    public SharedPreferences getPreferences(String name)
    {
        return null;
    }

    @Override
    public Application getApplication()
    {
        return null;
    }

    @Override
    public GLSurfaceView getSurfaceView()
    {
        return null;
    }

    @Override
    public ProcessManager getProcManager()
    {
        return null;
    }

    @Override
    public GLObjCache getGLObjCache()
    {
        return null;
    }

    @Override
    public IGameState getGameState()
    {
        return null;
    }

    @Override
    public IGameState getPrevGameState()
    {
        return null;
    }

    @Override
    public IGameState getPrevPrevGameState()
    {
        return null;
    }

    @Override
    public IGameState getOnPauseGameState()
    {
        return null;
    }

    @Override
    public IPhysics getPhysics()
    {
        return null;
    }

    @Override
    public void stopVirtualTime()
    {

    }

    @Override
    public void startVirtualTime()
    {

    }

    @Override
    public long getLastMeasuredTime()
    {
        return 0;
    }

    @Override
    public long getRealTime()
    {
        return 0;
    }

    @Override
    public void setVirtualTimeFactor(float factor)
    {

    }

    @Override
    public float getVirtualTimeFactor()
    {
        return 0;
    }

    @Override
    public void vibrate(long time)
    {

    }

    @Override
    public void showToast(String msg)
    {

    }

    @Override
    public GameObj addGameObj(GameObj obj)
    {
        return null;
    }

    @Override
    public void removeGameObj(GameObj obj)
    {

    }

    @Override
    public GameObj getObjById(int id)
    {
        return null;
    }

    @Override
    public GameObj getObjByListIdx(int idx)
    {
        return null;
    }

    @Override
    public GameObj getObjByListIdx(int idx, GameObj.GameObjLayer layer)
    {
        return null;
    }

    @Override
    public int getGameObjCnt()
    {
        return 0;
    }

    @Override
    public BaseGameView addGameView(BaseGameView view)
    {
        return null;
    }

    @Override
    public void removeGameView(BaseGameView view)
    {

    }

    @Override
    public void setOnPauseGameState(IGameState state)
    {

    }

    @Override
    public Activity getActivity()
    {
        return null;
    }

    @Override
    public void showMessage(String msg, String buttonText, DialogInterface.OnClickListener listener)
    {

    }
}
