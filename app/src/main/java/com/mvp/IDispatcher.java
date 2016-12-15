package com.mvp;

public interface IDispatcher<P> {
    void to(Class<? extends IMvpPresenter<?>>... targets);
    void toAny();
    IDispatcher<P> dispatchEvent(P data);
}
