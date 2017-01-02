package com.mvp;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.OnEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

public abstract class MvpPresenter<V extends MvpView> implements IMvpPresenter<V> {

    private V view;

    @Inject
    IMvpEventBus eventBus;

    @Inject
    Handler handler;

    @Inject
    ExecutorService executorService;

    Map<String, Future<?>> tasks = Collections.synchronizedMap(new HashMap<String, Future<?>>());
    private boolean destroyed;

    List<OnEventListener<?>> registeredEventListeners = new ArrayList<>();

    public MvpPresenter() { }

    private void unregisterEventListeners() {
        for (OnEventListener<?> eventListener : registeredEventListeners){
            eventBus.removeEventListener(eventListener);
            eventListener.onDestroy();
        }
        registeredEventListeners.clear();
    }

    <P, T extends OnEventListener<P>> void addEventListener(T eventListener) {
        if (eventBus.addEventListener(eventListener))
            registeredEventListeners.add(eventListener);
    }

    public <P> Dispatcher<P> dispatchEvent(P data){
        Dispatcher<P> dispatcher = new Dispatcher<>(eventBus);
        dispatcher.dispatchEvent(data);
        return dispatcher;
    }

    public V getView() {
        return view;
    }

    @Override
    public void onDestroyed() {
        if (!destroyed) {
            destroyed = true;
            for (Map.Entry<String,Future<?>> entry : tasks.entrySet())
                entry.getValue().cancel(true);
            unregisterEventListeners();
            tasks.clear();
            executorService.shutdown();
            this.view = null;
            handler.removeCallbacksAndMessages(null);
        }
    }

    protected void submitOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    protected void submitOnUiThread(Runnable runnable, int delay) {
        handler.postDelayed(runnable, delay);
    }

    @Override
    @BackgroundThread
    public void onInitialize() {
        Events.bind(this, eventBus, handler, executorService);
    }

    @Override
    public void onViewsInitialized() {

    }

    protected synchronized void tryCancelTask(String key) {
        if (tasks.containsKey(key)) {
            tasks.get(key).cancel(true);
            tasks.remove(key);
        }
    }

    public void submit(final String taskId, final Runnable runnable){
        tryCancelTask(taskId);
        tasks.put(taskId, executorService.submit(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                if (tasks.containsKey(taskId)) {
                    tasks.get(taskId).cancel(true);
                    tasks.remove(taskId);
                }
            }
        }));
    }

    @Override
    public void setView(V view) {
        this.view = view;
    }

}
