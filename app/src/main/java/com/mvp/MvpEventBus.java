package com.mvp;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.mvp.annotation.OnEventListener;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

public class MvpEventBus implements IMvpEventBus {

    private static final boolean DEBUG = true;

    private HashMap<Class<?>, OnEventListener<?>> eventListeners = new HashMap<>();

    public MvpEventBus() {}

    @Override
    public synchronized <V, T extends OnEventListener<V>>  boolean addEventListener(T eventListener) {
        boolean actuallyAdded = false;
        Class<?> clazz = eventListener.getDataClass();
        if (DEBUG) Log.e(MvpEventBus.class.getName(), clazz.toString());
        if (!eventListeners.containsKey(clazz)){
            eventListeners.put(clazz, eventListener);
            actuallyAdded = true;
        }else{
            OnEventListener<?> listener = eventListeners.get(clazz);

            if (listener == eventListener)
                return false;

            boolean valid = true;
            while(listener.hasNext()) {
                listener = listener.getNext();
                if (listener == eventListener){
                    valid = false;
                    break;
                }
            }
            if (valid) {
                listener.setNext(eventListener);
                actuallyAdded = true;
                printNodeTree("AFTER ADD: %s", eventListeners.get(clazz));
            }
        }

        return actuallyAdded;
    }

    @Override
    public synchronized <V, T extends OnEventListener<V>> boolean removeEventListener(T eventListenerWrapper) {
        Class<V> dataClass = eventListenerWrapper.getDataClass();
        if (DEBUG) Log.e(MvpEventBus.class.getName(), "searching for listener: " + eventListenerWrapper.toString());
        boolean actuallyRemoved = false;
        if (DEBUG) printNodeTree("linked list before removal: %s", eventListeners.get(dataClass));
        if (eventListeners.containsKey(dataClass)){
            OnEventListener<?> previous = eventListeners.get(dataClass);
            if (DEBUG) Log.e(MvpEventBus.class.getName(), "found listener in map: " + previous.toString());
            if (previous == eventListenerWrapper && !previous.hasNext()){
                if (DEBUG) Log.e(MvpEventBus.class.getName(), "removing listener: " + previous.toString());
                eventListeners.remove(dataClass);
                actuallyRemoved = true;
            }else if (previous == eventListenerWrapper && previous.hasNext()) {
                if (DEBUG) Log.e(MvpEventBus.class.getName(), "removing listener: " + previous.toString());
                eventListeners.remove(dataClass);
                eventListeners.put(dataClass, previous.getNext());
                previous.clearNext();
                actuallyRemoved = true;
            }else {
                OnEventListener<?> test = previous;
                if (DEBUG) Log.e(MvpEventBus.class.getName(), "traversing nodes... starting at: " + test.toString());
                while (test != null && test != eventListenerWrapper && test.hasNext()) {
                    previous = test;
                    if (DEBUG) Log.e(MvpEventBus.class.getName(), "now previous: " + previous.toString());
                    test = test.getNext();
                    if (DEBUG) Log.e(MvpEventBus.class.getName(), "now current: " + test.toString());
                }
                if (test == eventListenerWrapper) {
                    if (DEBUG) Log.e(MvpEventBus.class.getName(), "removing listener after traversal: " + eventListenerWrapper.toString());
                    previous.setNext(eventListenerWrapper.hasNext() ? eventListenerWrapper.getNext() : null);
                    eventListenerWrapper.clearNext();
                    actuallyRemoved = true;
                }else{
                    if (DEBUG) Log.e(MvpEventBus.class.getName(), "could not find listener after traversal: " + eventListenerWrapper.toString());
                }
            }
        }

        if (DEBUG) printNodeTree("linked list after removal: %s", eventListeners.get(dataClass));

        if (DEBUG) {
            int size = eventListeners.size();
            Log.e(MvpEventBus.class.getName(), String.format("should print 0 at the end: %s", size));

            if (size == 1) {
                for (Map.Entry<Class<?>, OnEventListener<?>> entry : eventListeners.entrySet()) {
                    Log.e(MvpEventBus.class.getName(), String.format("remaining listener class: ", entry.getKey().toString()));
                    Log.e(MvpEventBus.class.getName(), String.format("remaining listener: ", entry.getValue().toString()));
                    printNodeTree("Node Tree for remaining listener class %s: ", entry.getValue());
                }
            }
        }

        if (!actuallyRemoved)
            throw new IllegalStateException("das sollte nicht passieren...");

        return actuallyRemoved;
    }

    private void printNodeTree(String key, OnEventListener<?> onEventListener) {
        if (onEventListener == null){
            Log.e(MvpEventBus.class.getName(), String.format(key, "NULL"));
        }else {
            OnEventListener<?> traversal = onEventListener;
            StringBuilder sb = new StringBuilder();
            sb.append(traversal.toString());
            while (traversal.hasNext()) {
                traversal = traversal.getNext();
                sb.append(" --> ");
                sb.append(traversal.toString());
            }
            Log.e(MvpEventBus.class.getName(), String.format(key, sb.toString()));
        }
    }

    @Override
    public synchronized <V> void dispatchEvent(V data, Class<? extends IMvpPresenter<?>>... targets) {
        if (targets.length == 0) targets = null;
        Class<?> clazz = data.getClass();
        Log.d(getClass().getName(), clazz.toString());
        OnEventListener<V> eventListener = (OnEventListener<V>) eventListeners.get(clazz);
        if (eventListener != null)
            eventListener.onEvent(data, targets);
        while (!(clazz = clazz.getSuperclass()).equals(Object.class)){
            eventListener = (OnEventListener<V>) eventListeners.get(clazz);
            if (eventListener != null)
                eventListener.onEvent(data, targets);
        }
    }

}
