package com.mvp;

interface IMvpPresenter<V extends MvpView> {
    void onInitialize();
    void onDestroyed();
    void onViewsInitialized();
    void onViewAttached(V view);
    void onViewReattached(V view);
    void onViewDetached(V view);
    V getView();
    void setView(V view);
}
