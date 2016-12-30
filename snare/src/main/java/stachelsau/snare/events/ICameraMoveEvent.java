package stachelsau.snare.events;

public interface ICameraMoveEvent extends IEvent {

    public static final SystemEventType EVENT_TYPE = SystemEventType.UI_CAMERA_MOVE;
    
    float[] getCamPosition();
    void setCamPosition(float x, float y, float z);
    
}
