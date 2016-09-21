package com.mvp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class MvpPresenterFactory<V extends MvpView, T extends IMvpPresenter<V>> implements IMvpPresenterFactory<V, T> {

    @SuppressWarnings("unchecked")
    public final T build() {
        T presenterImpl = create();
        try {
            Class<?> clazz = Class.forName("com.mvp.annotation.processor." + presenterImpl.getClass().getSimpleName() + "Proxy");
            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            T presenterProxy = (T) constructor.newInstance(presenterImpl);
            presenterProxy.onInitialize();
            return presenterProxy;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("should not happen...");
    }
}
