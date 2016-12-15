package com.mvp;

public interface IMvpPresenterFactory<V extends MvpView, T extends MvpPresenter<V>> {
    T create();
}
