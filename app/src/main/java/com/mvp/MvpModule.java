package com.mvp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MvpModule<V extends MvpView> {

    protected final AppCompatActivity activity;
    protected final V view;

    public MvpModule(AppCompatActivity activity, V view){
        this.activity = activity;
        this.view = view;
    }

    @Provides
    public V getView() {
        return view;
    }

    @Provides
    public ExecutorService getBackgroundExecutorService() {
        return (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;
    }

    @Provides
    public Handler getMainHandler(){
        return new Handler(Looper.getMainLooper());
    }

    @Provides
    public LoaderManager getSupportLoaderManager() {
        return activity.getSupportLoaderManager();
    }

    @Provides
    @Named("presenterContext")
    public Context getApplicationContext(){
        return activity.getApplicationContext();
    }

}
