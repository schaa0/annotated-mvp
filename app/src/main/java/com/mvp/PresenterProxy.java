package com.mvp;

import android.os.Looper;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.UiThread;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PresenterProxy<V extends MvpView, P extends IMvpPresenter<V>> {

    final P presenterImpl;

    public PresenterProxy(P presenterImpl){
        this.presenterImpl = presenterImpl;
    }

    @SuppressWarnings("unchecked")
    public <T extends IMvpPresenter<V>> T create(final Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandlerImpl(presenterImpl, clazz));
    }

    private static class InvocationHandlerImpl<V extends MvpView, P extends IMvpPresenter<V>> implements InvocationHandler {

        private final P presenterImpl;
        private final Class<?> clazz;

        public InvocationHandlerImpl(P presenter, Class<?> clazz){
            this.presenterImpl = presenter;
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object o, Method method, final Object[] args) throws Throwable {
            boolean methodFromProxiedInterface = method.getDeclaringClass().isAssignableFrom(clazz);
            final Method methodImpl = presenterImpl.getClass().getMethod(method.getName(), method.getParameterTypes());
            synchronized (this) {
                if (methodFromProxiedInterface) {
                    boolean isUiThread = Looper.myLooper() == Looper.getMainLooper();
                    if (methodImpl.isAnnotationPresent(UiThread.class) && !isUiThread) {
                        invokeOnUiThread(args, methodImpl);
                    } else if (methodImpl.isAnnotationPresent(BackgroundThread.class) && isUiThread) {
                        invokeOnBackgroundThread(args, methodImpl);
                    } else {
                        return methodImpl.invoke(presenterImpl, args);
                    }
                    return null;
                }else{
                    return methodImpl.invoke(presenterImpl, args);
                }
            }
        }

        private void invokeOnBackgroundThread(final Object[] args, final Method methodImpl) {
            String methodName = methodImpl.getName();
            presenterImpl.tryCancelTask(methodName);
            presenterImpl.submit(methodName, new Runnable() {
                @Override
                public void run() {
                    try {
                        methodImpl.invoke(presenterImpl, args);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void invokeOnUiThread(final Object[] args, final Method methodImpl) {
            presenterImpl.submitOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        methodImpl.invoke(presenterImpl, args);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
