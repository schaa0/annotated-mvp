package com.mvp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public abstract class MvpActivityDelegate<V extends MvpView, P extends IMvpPresenter<V>> implements LoaderManager.LoaderCallbacks<P>   {

    private static final String KEY_INSTANCE_ID = "KEY_INSTANCE_ID";
    private final LoaderManager loaderManager;
    private Context context;
    private V view;

    private P presenter;
    private int id = -1;

    IMvpEventBus eventBus = MvpEventBus.get();
    private boolean loadFinishedCalled = false;
    private boolean onViewsInitializedCalled;
    private boolean firstDelivery = true;

    public MvpActivityDelegate(V view, Context context, LoaderManager loaderManager){
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
            getPresenter().onViewsInitialized();
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
        eventBus = null;
        context = null;
    }

    public P getPresenter() {
        return presenter;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(context.getApplicationContext(), new MvpPresenterFactory<V, P>() {
            @Override
            public P create() {
                return MvpActivityDelegate.this.create(eventBus);
            }
        });
    }

    protected abstract P create(IMvpEventBus eventBus);

    @Override
    public void onLoadFinished(Loader<P> loader, P presenter) {
        if (!loadFinishedCalled) {
            loadFinishedCalled = true;
            this.presenter = presenter;
            presenter.setView(view);
            if (firstDelivery)
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