package com.mvp;

interface IMvpPresenter<V extends MvpView> {
    void onInitialize();
    void onViewsInitialized();
    void onViewAttached(V view);
    void onViewReattached(V view);
    void onViewDetached(V view);
    V getView();
    void onDestroyed();
    void submitOnUiThread(Runnable runnable);
    void tryCancelTask(String methodName);
    void submit(String methodName, Runnable runnable);
    void setView(V view);
}
