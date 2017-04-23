package com.moonymango.snare.game;

/**
 * Provides a reference to the game instance, so everything that needs access to {@link IGame} should be
 * derived from this class.
 */

public class BaseSnareClass
{
    public final IGame mGame;

    public BaseSnareClass(IGame game)
    {
        if (game == null)
            throw new IllegalArgumentException("game reference must not be null");
        mGame = game;
    }

}
