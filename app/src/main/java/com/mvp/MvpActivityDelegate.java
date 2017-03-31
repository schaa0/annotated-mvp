package com.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class MvpActivityDelegate<V extends MvpView, P extends MvpPresenter<V>> implements LoaderManager.LoaderCallbacks<P>   {

    public static final String KEY_INSTANCE_ID = "KEY_INSTANCE_ID";
    private LoaderManager loaderManager;
    private IMvpEventBus eventBus;
    private PresenterComponent<V, P> component;
    private Context context;
    private V view;

    private P presenter;
    private int id = -1;

    private boolean loadFinishedCalled = false;
    private boolean onViewsInitializedCalled;
    private boolean firstDelivery = true;

    protected OnPresenterLoadedListener<V, P> onPresenterLoadedListener;

    public void setOnPresenterLoadedListener(OnPresenterLoadedListener<V, P> onPresenterLoadedListener) {
        this.onPresenterLoadedListener = onPresenterLoadedListener;
    }

    public MvpActivityDelegate(IMvpEventBus eventBus, PresenterComponent<V, P> component, V view, Context context, LoaderManager loaderManager){
        this.eventBus = eventBus;
        this.component = component;
        this.view = view;
        this.context = context;
        this.loaderManager = loaderManager;
    }

    public void onCreate(Bundle savedInstanceState) {
        firstDelivery = savedInstanceState == null;
        id = savedInstanceState == null ? hashCode() : savedInstanceState.getInt(KEY_INSTANCE_ID);
        loaderManager.initLoader(id, null, this);
    }

    public void onPostResume(){
        if (!onViewsInitializedCalled) {
            onViewsInitializedCalled = true;
            getPresenter().onNavigationEnabled();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_INSTANCE_ID, id);
    }

    public void onDestroy() {
        if (presenter != null) {
            presenter.onViewDetached(view);
            presenter.setView(null);
        }
        view = null;
        context = null;
        eventBus = null;
        component = null;
        loaderManager = null;
        presenter = null;
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
            if (onPresenterLoadedListener != null){
                onPresenterLoadedListener.onPresenterLoaded(presenter);
            }
            presenter.setIsReattached(!this.firstDelivery);
            if (this.firstDelivery)
                presenter.onViewAttached(view);
            else
                presenter.onViewReattached(view);
        }
    }

    @Override
    public void onLoaderReset(Loader<P> loader) {
        presenter = null;
    }
}
