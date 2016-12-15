package com.mvp;

/**
 * Created by Andy on 07.12.2016.
 */

public abstract class MockablePresenterComponent<V extends MvpView, T extends MvpPresenter<V>> implements PresenterComponent<V, T>{

    private final V view;

    public MockablePresenterComponent(V view){
        this.view = view;
    }

    @Override
    public V view() {
        return view;
    }
}
