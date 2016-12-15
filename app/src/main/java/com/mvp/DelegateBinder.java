package com.mvp;

import android.os.Bundle;

import com.mvp.MvpPresenter;
import com.mvp.MvpView;

/**
 * Created by Andy on 13.12.2016.
 */
public interface DelegateBinder<V extends MvpView, P extends MvpPresenter<V>> {
    void onCreate(Bundle savedInstanceState);
    void onPostResume();
    void onDestroy();
    void onSaveInstanceState(Bundle outState);
    P getPresenter();
}
