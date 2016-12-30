package stachelsau.snare.events;

/**
 * Generic event.
 */
public interface IUserEvent extends IEvent {
    public static final SystemEventType EVENT_TYPE = SystemEventType.USER_EVENT;
    Object getData();
    void setUserData(Object data);
}
