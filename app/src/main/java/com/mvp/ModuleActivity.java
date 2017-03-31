package com.mvp;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 30.03.2017.
 */
@Module
public class ModuleActivity
{
    private AppCompatActivity activity;

    public ModuleActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Provides
    public AppCompatActivity activity() {
        return this.activity;
    }

    @Provides
    public FragmentManager fragmentManager(AppCompatActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @Provides
    public LoaderManager loaderManager(AppCompatActivity activity) {
        return activity.getSupportLoaderManager();
    }

}
