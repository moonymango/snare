package stachelsau.snare.events;

/** Distributes status information. */
public interface IStatsUpdateEvent extends IEvent 
{
    public static final SystemEventType EVENT_TYPE = 
            SystemEventType.STATS_UPDATE;
    
    /** Returns actual measurement. */
    float getFramesPerSecond();
    /** Returns previous measurement. */
    float getPrevFramesPerSecond();
    /** Gets max delta time between frames in fps measurement interval. */
    float getMaxDelta();
    /** Gets min delta time between frames in fps measurement interval. */
    float getMinDelta();
    /** 
     * Sets fps data.
     * @param fps Actual fps measurement.
     * @param prev Previous fps measurement.
     */
    void setStatsData(float fps, float prev, float minDelta, float maxDelta);
}
