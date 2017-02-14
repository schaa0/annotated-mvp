package com.mvp;

public interface EventBus {
    void register(Object o);
    void unregister(Object o);
    <V> IDispatcher<V> dispatchEvent(V data);
}
