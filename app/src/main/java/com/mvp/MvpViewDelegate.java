package com.mvp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class MvpViewDelegate<V extends MvpViewInLayout<P>, P extends MvpPresenter<V>> implements LoaderManager.LoaderCallbacks<P> {

    public static final int CONTAINER_ACTIVITY = 0x1;
    public static final int CONTAINER_FRAGMENT = 0x2;

    private IMvpEventBus eventBus;
    private final PresenterComponent<V, P> component;

    private Context context;
    private final int containerType;
    private final String viewId;
    private final LoaderManager loaderManager;

    private V view;
    private boolean firstDelivery = true;
    private Bundle savedState;
    private boolean alreadyRestored = false;

    public MvpViewDelegate(IMvpEventBus eventBus, PresenterComponent<V, P> component, LoaderManager loaderManager, Context context, @IdRes int viewId, int containerType){
        this.eventBus = eventBus;
        this.component = component;
        this.loaderManager = loaderManager;
        this.context = context;
        this.containerType = containerType;
        this.viewId = String.valueOf(viewId);
    }

    private P presenter;
    private int id = -1;

    private boolean loadFinishedCalled = false;

    public void attachView(Bundle savedInstanceState, V view){
        this.view = view;
        this.savedState = savedInstanceState;
        firstDelivery = savedInstanceState == null;
        id = savedInstanceState == null ? hashCode() : savedInstanceState.getInt(viewId);
        /*if (!firstDelivery && !alreadyRestored){
            internalOnRestoreInstanceState(savedInstanceState);
        }*/
        loaderManager.initLoader(id, null, this);
    }

    public void onSaveInstanceState(Bundle outState) {
        alreadyRestored = false;
        outState.putInt(viewId, id);
        if (view instanceof MvpViewWithStateInLayout){
            ((MvpViewWithStateInLayout) view).onSaveInstanceState(outState);
        }
    }

    public void onRestoreInstanceState(Bundle savedState){
        this.savedState = savedState;
        internalOnRestoreInstanceState(savedState);
    }

    private void internalOnRestoreInstanceState(Bundle savedState) {
        if (!alreadyRestored && view instanceof MvpViewWithStateInLayout && savedState != null && view != null){
            ((MvpViewWithStateInLayout) view).onRestoreInstanceState(savedState);
            alreadyRestored = true;
        }
    }

    public void onDestroy() {
        if (presenter != null) {
            presenter.setView(null);
            presenter.onViewDetached(view);
        }
        view = null;
        context = null;
        savedState = null;
    }

    public P getPresenter() {
        return presenter;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(context.getApplicationContext(), new MvpPresenterFactory<V, P>(eventBus) {
            @Override
            public P create() {
                return component.newInstance();
            }
        });
    }

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
                internalOnRestoreInstanceState(savedState);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        presenter = null;
    }

}
