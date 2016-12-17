package com.mvp;

/**
 * Created by Andy on 16.12.2016.
 */

public interface OnPresenterLoadedListener<V extends MvpView, P extends MvpPresenter<V>> {
    void onPresenterLoaded(P presenter);
}
