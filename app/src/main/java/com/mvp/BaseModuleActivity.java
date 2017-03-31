package com.mvp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.ActivityScope;

import dagger.Module;
import dagger.Provides;

@Module
public class BaseModuleActivity
{
    private AppCompatActivity activity;

    public BaseModuleActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    public AppCompatActivity activity() {
        return this.activity;
    }

    @Provides
    @ActivityScope
    public FragmentManager fragmentManager(AppCompatActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @Provides
    @ActivityScope
    public LoaderManager loaderManager(AppCompatActivity activity) {
        return activity.getSupportLoaderManager();
    }

}
