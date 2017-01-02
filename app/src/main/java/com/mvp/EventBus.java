package com.mvp;

import android.os.Handler;

import java.util.concurrent.ExecutorService;

public interface EventBus {
    void register(Object o);
    void unregister(Object o);
    <V> IDispatcher<V> dispatchEvent(V data);
}
