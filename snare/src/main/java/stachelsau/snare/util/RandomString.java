package stachelsau.snare.util;

import stachelsau.snare.game.Game;

/**
 * taken from:
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string-in-java
 */
public class RandomString {
    
    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    private static final char[] symbols = new char[36];

    static {
      for (int idx = 0; idx < 10; ++idx)
        symbols[idx] = (char) ('0' + idx);
      for (int idx = 10; idx < 36; ++idx)
        symbols[idx] = (char) ('a' + idx - 10);
    }
    
    // ---------------------------------------------------------
    // fields
    // ---------------------------------------------------------
    private final Game mGame;
    private final char[] buf;
    

    // ---------------------------------------------------------
    // constructors
    // ---------------------------------------------------------
    public RandomString(int length, Game game)
    {
      if (length < 1)
        throw new IllegalArgumentException("length < 1: " + length);
      if (game == null) {
          throw new IllegalArgumentException("Missing game instance.");
      }
      buf = new char[length];
      mGame = game;
    }
    
    // ---------------------------------------------------------
    // methods
    // ---------------------------------------------------------
    public String nextString()
    {
      for (int idx = 0; idx < buf.length; ++idx) 
        buf[idx] = symbols[mGame.getRandomInt(0, symbols.length - 1)];
      return new String(buf);
    }

    // ---------------------------------------------------------
    // overrides
    // ---------------------------------------------------------

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
