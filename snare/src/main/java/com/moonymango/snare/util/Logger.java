package com.moonymango.snare.util;

import com.moonymango.snare.game.Game;
import android.util.Log;

public class Logger {

    //---------------------------------------------------------
    // static
    //---------------------------------------------------------
    public static final String TAG = Game.ENGINE_NAME;
    public static boolean sMasterEnable = true;
    public static LogLevel sCurrentLevel = LogLevel.INFO; 
     
    public static void i(LogSource source, String msg) {
        if (!sMasterEnable || !source.mEnabled) return;
        if (sCurrentLevel.covers(LogLevel.INFO)) {
            Log.i(TAG, String.format("%s: %s", source.name(), msg));
        }
    }
    
    public static void d(LogSource source, String msg) {
        if (!sMasterEnable || !source.mEnabled) return;
        if (sCurrentLevel.covers(LogLevel.DEBUG)) {
            Log.d(TAG, String.format("%s: %s", source.name(), msg));
        }
    }
    
    public static void w(LogSource source, String msg) {
        if (!sMasterEnable || !source.mEnabled) return;
        if (sCurrentLevel.covers(LogLevel.WARNING)) {
            Log.w(TAG, String.format("%s: %s", source.name(), msg));
        }
    }
    
    public static void e(LogSource source, String msg) {
        if (!sMasterEnable || !source.mEnabled) return;
        if (sCurrentLevel.covers(LogLevel.ERROR)) {
            Log.e(TAG, String.format("%s: %s", source.name(), msg));
        }
    }
    
    public static void wtf(LogSource source, String msg) {
        if (!sMasterEnable || !source.mEnabled) return;
        if (sCurrentLevel.covers(LogLevel.FAILURE)) {
            Log.wtf(TAG, String.format("%s: %s", source.name(), msg));
        }
    }
    
    public enum LogSource {
        EVENTS          (false),
        GAME            (false),
        FPS             (true),
        RESOURCES       (true),
        OPENGL          (true),
        OPENGL_TRACE    (false),
        UTILS           (true),
        SOUND           (true),
        MISC            (true),
        PROFILER        (true);
        
        public final boolean mEnabled;
        LogSource(boolean enabled) {
            mEnabled = enabled;
        }
    }
    
    public enum LogLevel {
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FAILURE;
        
        boolean covers(LogLevel level) {
            return level.ordinal() >= this.ordinal();
        }
    }
    
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

    // ---------------------------------------------------------
    // classes + interfaces
    // ---------------------------------------------------------
}
