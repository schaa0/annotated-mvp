package com.mvp;

public interface PresenterComponent<V extends MvpView, T extends MvpPresenter<V>> {
    T newInstance();
    V view();
}
