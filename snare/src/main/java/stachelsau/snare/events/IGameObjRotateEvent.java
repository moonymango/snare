package stachelsau.snare.events;


public interface IGameObjRotateEvent extends IEvent {
    public static final SystemEventType EVENT_TYPE = SystemEventType.GAMEOBJ_ROTATE;
    
    int getGameObjID();
    void setGameObjData(int id);
}
