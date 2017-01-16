package com.moonymango.snare.events;

import java.util.ArrayList;
import java.util.HashMap;

import com.moonymango.snare.util.Pool;


/**
 * This the only thread-safe class in snare, so you can fire events from
 * outside the game loop, i.e. from the ui thread. 
 */
public class EventManager {
    
    private int mListenerCnt;
    private final HashMap<IEventType, ArrayList<IEventListener>> mEventListenerMap = new HashMap<>();
    
    private final ArrayList<DefaultEvent> mEventQueueA = new ArrayList<>();
    private final ArrayList<DefaultEvent> mEventQueueB = new ArrayList<>();
    private ArrayList<DefaultEvent> mFrontQueue = mEventQueueA;
    private ArrayList<DefaultEvent> mBackQueue = mEventQueueB;
    
    private final Object mLockListeners = new Object(); // monitor for listener map
    private final Object mLockQueues = new Object();    // monitor for queues lists
   
    private final Pool<? extends DefaultEvent> mEventPool;
           
    public EventManager(Pool<? extends DefaultEvent> pool) {
        if (pool == null)
            throw new IllegalArgumentException("Missing event pool.");
        mEventPool = pool;
    }
    
    public DefaultEvent obtain(IEventType type) {
        DefaultEvent e;
        synchronized (mEventPool) {
            e = mEventPool.obtain();    
        }
        e.setType(type);
        return e;
    }
        
     /**
     * Adds a listener for the given event type.
     * @param evtType Event type.
     * @param listener Event listener.
     * @return True if listener was added, false otherwise.
     */
    public boolean addListener(IEventType evt, IEventListener listener) {
        if (evt == null || listener == null)
            throw new IllegalArgumentException();
        
        synchronized (mLockListeners) {
            // get list for event type
            ArrayList<IEventListener> list = mEventListenerMap.get(evt);
            if (list == null) {
                // new event type, create listener's list
                //Logger.i(LogSource.EVENTS, "register new event type: " + evt.getName());
                list = new ArrayList<IEventListener>();
                mEventListenerMap.put(evt, list);
            } 
            
            if (list.contains(listener)) return false;  // check for duplicates
            list.add(listener);
        }
        
        ++mListenerCnt;
        return true;
    }
    

    /**
     * Removes an event listener.
     * @param evtType Type of event.
     * @param listener Event listener.
     * @return True if listener was removed, false otherwise.
     */
    public boolean removeListener(IEventType evt, IEventListener listener) {
        if (evt == null || listener == null)
            throw new IllegalArgumentException();
        
        // get list for event type
        synchronized (mLockListeners) {
            ArrayList<IEventListener> list = mEventListenerMap.get(evt);
            if (list == null) return false;
            final boolean result = list.remove(listener);
            if (result) {
                --mListenerCnt;
            }
            return result;
        }
    }
    

    public int getListenerCnt() {
        return mListenerCnt;
    }
    
    /**
     * For debugging purposes only. Do NOT modify the returned list!
     * @param type
     * @return
     */
    public ArrayList<IEventListener> getListeners(IEventType type) {
        return mEventListenerMap.get(type);
    }
    
    /**
     * Queue an event for asynchronous handling. Listeners
     * will be notified of this event in the next frame of the 
     * game loop.
     * @param event Event
     * @return True if event was queued.
     */
    public boolean queueEvent(IEvent evt) {
        if (evt == null) return false;
        
        synchronized (mLockQueues) {
            mFrontQueue.add((DefaultEvent) evt);
        }
        return true;
    }

    
    /**
     * Trigger immediate handling for an event. 
     * @param event Event.
     */
    public void triggerEvent(IEvent evt) {
        final ArrayList<IEventListener> list = mEventListenerMap.get(evt.getType());
        synchronized (mLockListeners) {

            if (list != null) {
                final int len = list.size();
                for (int i = 0; i < len; i++) {
                    if (list.get(i).handleEvent(evt)) {
                        break;
                    }
                }
            }
        }
        
        synchronized(mEventPool) {
            evt.recycle();
        }
    }
    
    /**
     * Handle events in queue.
     * @param millis No effect.
     * @return Always true.
     */
    public boolean tick(long millis) {
        // todo: implement time limit    
        synchronized (mLockQueues) {
            swapQueues();
            int len = mBackQueue.size();
            for (int i = 0; i < len; i++) {
                triggerEvent(mBackQueue.get(i));
            }
        }
        return true;
    }
    
    private void swapQueues() {
        if (mFrontQueue == mEventQueueA) {
            mFrontQueue = mEventQueueB;
            mBackQueue = mEventQueueA;
        } else {
            mFrontQueue = mEventQueueA;
            mBackQueue = mEventQueueB;
        }
        mFrontQueue.clear();
    }
    
     
    public interface IEventListener {
        /**
         * Callback to handle the event. 
         * Attention: DO NOT add/remove listeners in this method, e.g. 
         * add/remove game objects!  
         * 
         * @param event
         * @return true, if the event should not be delivered to further 
         * listeners, false otherwise
         */
        boolean handleEvent(IEvent event);
    }
    
    /**
     * Normally this is intended to be implemented by an Enum, for it's 
     * going to be used as HashMap key in the EventManager. However if it is
     * implemented by a non-enum class, the methods hashCode and equals have to be
     * overridden so that different instances that represent the same event type 
     * return the same hashCode.
     */
    public interface IEventType {  
        int hashCode(); 
        boolean equals(Object o);
        String getName();
    }
}
