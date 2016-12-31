package com.moonymango.snare.events;

/**
 * Generic event.
 */
public interface IUserEvent extends IEvent {
    SystemEventType EVENT_TYPE = SystemEventType.USER_EVENT;
    Object getData();
    void setUserData(Object data);
}
