package com.mvp;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.VisibleForTesting;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.OnEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class MvpPresenter<V extends MvpView> implements IMvpPresenter<V> {

    private V view;
    private IMvpEventBus eventBus;
    private Handler handler;
    Map<String, Future<?>> tasks = Collections.synchronizedMap(new HashMap<String, Future<?>>());
    private ExecutorService executorService;
    private boolean destroyed;

    private List<OnEventListener<?>> registeredEventListeners = new ArrayList<>();

    protected void unregisterEventListeners() {
        for (OnEventListener<?> eventListener : registeredEventListeners){
            eventBus.removeEventListener(eventListener);
            eventListener.onDestroy();
        }
        registeredEventListeners.clear();
    }

    public <P, T extends OnEventListener<P>> void addEventListener(T eventListener){
        if (eventBus.addEventListener(eventListener))
            registeredEventListeners.add(eventListener);
    }

    public <P> void dispatchEvent(P data, Class<? extends MvpPresenter<?>>... targets){
        eventBus.dispatchEvent(data, targets);
    }

    public MvpPresenter(){ }

    public MvpPresenter(IMvpEventBus eventBus){
        this(eventBus, Looper.getMainLooper(), Executors.newCachedThreadPool());
    }


    @VisibleForTesting
    public MvpPresenter(IMvpEventBus eventBus, Looper looper, ExecutorService executorService){
        super();
        this.eventBus = eventBus;
        this.executorService = executorService;
        this.handler = new UiThreadQueueHandler(looper);
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

    @Override
    public void submitOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    @Override
    @BackgroundThread
    public void onInitialize() {
        Events.bind(this, handler, executorService);
    }

    @Override
    public void onViewsInitialized() {

    }

    public synchronized void tryCancelTask(String key) {
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

    private static class UiThreadQueueHandler extends Handler {
        UiThreadQueueHandler(Looper looper) {
            super(looper);
        }
    }
}
