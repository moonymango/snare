package com.moonymango.snare.game;

/**
 * Base class for other classes.
 * Provides a reference to the game instance.
 */

public class BaseSnareClass
{
    public final IGame mGame;

    public BaseSnareClass(IGame game)
    {
        mGame = game;
    }

}
