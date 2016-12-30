package stachelsau.snare.util;

import stachelsau.snare.util.Logger.LogSource;
import android.os.SystemClock;

public class Profiler {
    
    private static String sMeasurementName;
    private static long sStart;
    
    public static void start(String measurementName) {
        sMeasurementName = measurementName;
        sStart = SystemClock.elapsedRealtime();
    }
    
    public static void stop() {
        final long time = SystemClock.elapsedRealtime() - sStart;
        Logger.i(LogSource.PROFILER, sMeasurementName + " " + time);
    }

}
