package com.mvp;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public abstract class MvpActivity<V extends MvpView, P extends MvpPresenter<V>> extends AppCompatActivity {

    private MvpActivityDelegate<V, P> delegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate = new ActivityDelegate();
        delegate.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        delegate.onPostResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
    }

    protected abstract V getMvpView();
    protected abstract P create(IMvpEventBus eventBus);

    private class ActivityDelegate extends MvpActivityDelegate<V, P> {

        public ActivityDelegate() {
            super(getMvpView(), getApplicationContext(), getSupportLoaderManager());
        }

        @Override
        protected P create(IMvpEventBus eventBus) {
            return MvpActivity.this.create(eventBus);
        }
    }

}
