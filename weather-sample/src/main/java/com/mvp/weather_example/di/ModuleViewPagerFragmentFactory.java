package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.weather_example.view.MainActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 21.01.2017.
 */

@Module
public class ModuleViewPagerFragmentFactory
{

    protected AppCompatActivity activity;

    public ModuleViewPagerFragmentFactory(AppCompatActivity activity){
        this.activity = activity;
    }

    @Provides
    public ViewPagerFragmentFactory fragmentFactory() {
        return new ViewPagerFragmentFactory();
    }

    @Provides
    public MainActivity.SectionsPagerAdapter sectionsPagerAdapter(ViewPagerFragmentFactory viewPagerFragmentFactory) {
        return new MainActivity.SectionsPagerAdapter(activity.getSupportFragmentManager(), viewPagerFragmentFactory);
    }


}
