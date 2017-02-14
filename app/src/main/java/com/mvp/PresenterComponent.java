package com.mvp;

/**
 * Created by Andy on 01.12.2016.
 */

public interface PresenterComponent<V extends MvpView, T extends MvpPresenter<V>> {
    T newInstance();
    V view();
}
