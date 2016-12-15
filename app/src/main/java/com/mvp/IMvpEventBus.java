package com.mvp;

import com.mvp.annotation.OnEventListener;

import javax.inject.Singleton;

@Singleton
public interface IMvpEventBus {
    <V, T extends OnEventListener<V>> boolean addEventListener(T eventListenerWrapper);
    <V, T extends OnEventListener<V>> boolean removeEventListener(T eventListenerWrapper);
    <V> void dispatchEvent(V data, Class<? extends IMvpPresenter<?>>... targets);
}
