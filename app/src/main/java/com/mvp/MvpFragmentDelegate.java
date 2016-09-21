package com.mvp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class MvpFragmentDelegate<V extends MvpView, P extends IMvpPresenter<V>> implements LoaderManager.LoaderCallbacks<P> {

    private Context context;
    private final String fragmentTag;
    private V view;
    IMvpEventBus eventBus = MvpEventBus.get();

    private boolean firstDelivery = true;

    public MvpFragmentDelegate(V view, Context context, String fragmentTag){
        this.view = view;
        this.context = context;
        this.fragmentTag = fragmentTag;
    }

    private P presenter;

    private boolean loadFinishedCalled = false;
    private int id = -1;
    private boolean onViewsInitializedCalled = false;

    public P getPresenter() {
        return presenter;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        firstDelivery = savedInstanceState == null;
        id = savedInstanceState == null ? hashCode() : savedInstanceState.getInt(fragmentTag);
    }

    public void onActivityCreated(LoaderManager loaderManager) {
        loaderManager.initLoader(id, null, this);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(fragmentTag, id);
    }

    public void onDestroyView() {
        if (presenter != null) {
            presenter.onViewDetached(view);
            presenter.setView(null);
        }
        view = null;
        context = null;
        eventBus = null;
    }

    @Override
    public Loader<P> onCreateLoader(int id, Bundle args) {
        return new PresenterLoader<>(context.getApplicationContext(), new MvpPresenterFactory<V, P>() {
            @Override
            public P create() {
                return MvpFragmentDelegate.this.create(eventBus);
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
        this.presenter = null;
    }

    public void onResume() {
        if (!onViewsInitializedCalled){
            onViewsInitializedCalled = true;
            presenter.onViewsInitialized();
        }
    }
}
