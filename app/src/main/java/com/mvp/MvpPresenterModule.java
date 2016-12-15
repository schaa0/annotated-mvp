package com.mvp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.Presenter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MvpPresenterModule<V extends MvpView, P extends MvpPresenter<V>> extends MvpModule<V> {

    protected final PresenterComponent<V, P> presenterComponent;

    public MvpPresenterModule(AppCompatActivity activity, V view, PresenterComponent<V, P> presenterComponent) {
        super(activity, view);
        this.presenterComponent = presenterComponent;
    }

    @Provides
    public PresenterComponent<V, P> buildPresenter(){
        return presenterComponent;
    }

}
