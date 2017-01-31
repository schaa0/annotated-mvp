package com.mvp;

import android.os.Handler;
import android.util.Log;

import com.mvp.annotation.OnEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

class Events {

    private static final String PACKAGE_PREFIX = "com.mvp.";

    private static Method m;

    static {
        try {
            m = Class.forName("com.mvp.MvpEventListener").getDeclaredMethod("get", String.class, Object.class, Handler.class, ExecutorService.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    static <V extends MvpView, T extends MvpPresenter<V>> void bind(T presenter, IMvpEventBus eventBus, Handler handler, ExecutorService executorService){
        synchronized (Events.class) {
            long begin = System.currentTimeMillis();
            String presenterClassName = presenter.getClass().getName();
            boolean isMock = false;
            if (presenterClassName.contains("$MockitoMock$")){
                presenterClassName = presenter.getClass().getSuperclass().getName();
                isMock = true;
            }
            try {
                ArrayList<OnEventListener<?>> onEventListeners =
                        (ArrayList<OnEventListener<?>>) m.invoke(null, presenterClassName, presenter, handler, executorService);
                for (OnEventListener<?> eventListener : onEventListeners) {
                    if (!isMock)
                        presenter.addEventListener(eventListener);
                    else
                        eventBus.addEventListener(eventListener);
                }
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            Log.e(Events.class.getName(), String.format("adding event listeners for presenter: %s took %d ms", presenterClassName, end - begin));
        }
    }

    static <V extends MvpView, T extends MvpPresenter<V>> ArrayList<OnEventListener<?>> bind(Object obj, EventBus eventBus, Handler handler, ExecutorService executorService){
        synchronized (Events.class) {
            IMvpEventBus mvpEventBus = (IMvpEventBus) eventBus;
            long begin = System.currentTimeMillis();
            String className = obj.getClass().getName();
            if (className.contains("$MockitoMock$")){
                className = obj.getClass().getSuperclass().getName();
            }
            try {
                ArrayList<OnEventListener<?>> onEventListeners =
                        (ArrayList<OnEventListener<?>>) m.invoke(null, className, obj, handler, executorService);
                for (OnEventListener<?> eventListener : onEventListeners) {
                    mvpEventBus.addEventListener(eventListener);
                }
                return onEventListeners;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            Log.e(Events.class.getName(), String.format("adding event listeners for presenter: %s took %d ms", className, end - begin));
            return new ArrayList<>();
        }
    }


}
