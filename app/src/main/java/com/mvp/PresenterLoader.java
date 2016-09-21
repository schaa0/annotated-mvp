package com.mvp;

import android.content.Context;
import android.support.v4.content.Loader;

class PresenterLoader<V extends MvpView, I extends IMvpPresenter<V>> extends Loader<I> {

    private final MvpPresenterFactory<V, I> factory;
    private I presenter;

    public PresenterLoader(Context context, MvpPresenterFactory<V, I> factory) {
        super(context);
        this.factory = factory;
    }

    @Override
    protected void onStartLoading() {
        if (presenter != null) {
            deliverResult(presenter);
        }else {
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        presenter = factory.build();
        deliverResult(presenter);
    }

    @Override
    protected void onReset() {
        presenter.onDestroyed();
        presenter = null;
    }
}
