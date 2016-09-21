package com.mvp;

public interface IMvpPresenterFactory<V extends MvpView, T extends IMvpPresenter<V>> {
    T create();
}
