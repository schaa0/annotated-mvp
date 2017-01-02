package com.mvp;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;

public abstract class MvpPresenterFactory<V extends MvpView, T extends MvpPresenter<V>> implements IMvpPresenterFactory<V, T> {

    private final IMvpEventBus eventBus;

    public MvpPresenterFactory(IMvpEventBus eventBus){
        this.eventBus = eventBus;
    }

    @SuppressWarnings("unchecked")
    public final T build() {
        T presenterImpl = create();
        try {
            String simpleName = presenterImpl.getClass().getSimpleName();
            if (simpleName.contains("$MockitoMock$")){
                Events.bind(presenterImpl, eventBus, new Handler(Looper.myLooper()), Executors.newSingleThreadExecutor());
                return presenterImpl;
            }else {
                Class<?> clazz = Class.forName(presenterImpl.getClass().getName() + "Proxy");
                Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
                T presenterProxy = (T) constructor.newInstance(presenterImpl);
                presenterProxy.onInitialize();
                return presenterProxy;
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
