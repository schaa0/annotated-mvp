package com.mvp;

import android.os.Handler;
import android.util.Log;

import com.mvp.annotation.OnEventListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class Events {

    private static final String PACKAGE_PREFIX = "com.mvp.annotation.processor.";

    private static Method m;

    static {
        try {
            Class<?> mvpEventListenerClass = Class.forName(PACKAGE_PREFIX + "MvpEventListener");
            m = mvpEventListenerClass.getDeclaredMethod("get", String.class, Object.class, Handler.class, ExecutorService.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <V extends MvpView, T extends MvpPresenter<V>> void bind(T presenter, Handler handler, ExecutorService executorService){
        synchronized (Events.class) {
            long begin = System.currentTimeMillis();
            String presenterClassName = presenter.getClass().getName();
            try {
                ArrayList<OnEventListener<?>> onEventListenersWrappers =
                        (ArrayList<OnEventListener<?>>) m.invoke(null, presenterClassName, presenter, handler, executorService);
                for (OnEventListener<?> eventListenerWrapper : onEventListenersWrappers) {
                    presenter.addEventListener(eventListenerWrapper);
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

}
