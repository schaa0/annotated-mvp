package com.mvp;

import android.support.annotation.Nullable;

/**
 * Created by Andy on 01.12.2016.
 */

public interface PresenterComponent<V extends MvpView, T extends MvpPresenter<V>> {
    T newInstance();
    V view();
}
