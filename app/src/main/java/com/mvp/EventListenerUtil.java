package com.mvp;

import android.os.Handler;

import com.mvp.annotation.OnEventListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;

public class EventListenerUtil {

    public static OnEventListener<?> createEventListener(Class<? extends IMvpPresenter<?>> presenterClass, Class<?> clazz, IMvpPresenter<?> presenter, Handler handler, ExecutorService service){
        try {
            Class<?> c = Class.forName(String.format("com.mvp.annotation.processor.%s__EventDelegate__%s", presenterClass.getSimpleName(), clazz.getSimpleName()));
            Constructor<?> constructor = c.getDeclaredConstructors()[0];
            return (OnEventListener<?>) constructor.newInstance(presenter, handler, service);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

}
