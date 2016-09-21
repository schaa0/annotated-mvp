package com.mvp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public abstract class MvpFragment<V extends MvpView, P extends IMvpPresenter<V>> extends Fragment {

    private MvpDelegate delegate;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate = new MvpDelegate();
        delegate.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        delegate.onActivityCreated(getLoaderManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        delegate.onDestroyView();
        super.onDestroyView();
    }

    protected P getPresenter(){
        return delegate.getPresenter();
    }

    protected abstract V getMvpView();
    protected abstract String getFragmentTag();
    protected abstract P create(IMvpEventBus eventBus);

    private class MvpDelegate extends MvpFragmentDelegate<V, P>{

        public MvpDelegate() {
            super(getMvpView(), getContext(), getFragmentTag());
        }

        @Override
        protected P create(IMvpEventBus eventBus) {
            return MvpFragment.this.create(eventBus);
        }

    }

}
