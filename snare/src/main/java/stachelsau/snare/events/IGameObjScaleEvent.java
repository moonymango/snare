package stachelsau.snare.events;


/**
 * Event: game obj scaled
 */
public interface IGameObjScaleEvent extends IEvent {
    
    public static final SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_SCALE;
    
    int getGameObjID();
    float[] getScale();
    void setGameObjData(int objID, float x, float y, float z);
}
