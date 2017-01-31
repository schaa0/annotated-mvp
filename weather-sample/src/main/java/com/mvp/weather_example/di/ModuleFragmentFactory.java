package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.weather_example.view.MainActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 21.01.2017.
 */

@Module
public class ModuleFragmentFactory
{

    protected AppCompatActivity activity;

    public ModuleFragmentFactory(AppCompatActivity activity){
        this.activity = activity;
    }

    @Provides
    public FragmentFactory fragmentFactory() {
        return new FragmentFactory();
    }

    @Provides
    public MainActivity.SectionsPagerAdapter sectionsPagerAdapter(FragmentFactory fragmentFactory) {
        return new MainActivity.SectionsPagerAdapter(activity.getSupportFragmentManager(), fragmentFactory);
    }


}
