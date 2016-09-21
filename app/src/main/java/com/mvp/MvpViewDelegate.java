package com.mvp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class MvpViewDelegate<V extends MvpViewInLayout<P>, P extends IMvpPresenter<V>> implements LoaderManager.LoaderCallbacks<P> {

    public static final int CONTAINER_ACTIVITY = 0x1;
    public static final int CONTAINER_FRAGMENT = 0x2;

    private Context context;
    private final int containerType;
    private final String viewId;
    private final LoaderManager loaderManager;

    private V view;
    private boolean firstDelivery = true;
    private Bundle savedState;

    public MvpViewDelegate(LoaderManager loaderManager, Context context, @IdRes int viewId, int containerType){
        this.loaderManager = loaderManager;
        this.context = context;
        this.containerType = containerType;
        this.viewId = String.valueOf(viewId);
    }

    private P presenter;
    private int id = -1;

    IMvpEventBus eventBus = MvpEventBus.get();
    private boolean loadFinishedCalled = false;

    public void attachView(Bundle savedInstanceState, V view){
        this.view = view;
        firstDelivery = savedInstanceState == null;
        id = savedInstanceState == null ? hashCode() : savedInstanceState.getInt(viewId);
        loaderManager.initLoader(id, null, this);
    }

    private boolean isActivityContainer() {
        return containerType == CONTAINER_ACTIVITY;
    }

    private boolean isFragmentContainer() {
        return containerType == CONTAINER_FRAGMENT;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(viewId, id);
    }

    public void onRestoreInstanceState(Bundle savedState){
        this.savedState = savedState;
    }

    public void onDestroy() {
        if (presenter != null) {
            presenter.setView(null);
            presenter.onViewDetached(view);
        }
        view = null;
        eventBus = null;
        context = null;
        savedState = null;
    }

    public P getPresenter() {
        return presenter;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(context.getApplicationContext(), new MvpPresenterFactory<V, P>() {
            @Override
            public P create() {
                return MvpViewDelegate.this.create(eventBus);
            }
        });
    }

    public abstract P create(IMvpEventBus eventBus);

    @Override
    public void onLoadFinished(Loader<P> loader, P presenter) {
        if (!loadFinishedCalled) {
            loadFinishedCalled = true;
            this.presenter = presenter;
            presenter.setView(view);
            view.setPresenter(presenter);
            if (firstDelivery) {
                presenter.onViewAttached(view);
            } else {
                presenter.onViewReattached(view);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        presenter = null;
    }

}
