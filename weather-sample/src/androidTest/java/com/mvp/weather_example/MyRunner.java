package com.mvp.weather_example;

import android.app.Application;

import com.mvp.weather_example.di.ModuleProvider;

/**
 * Created by Andy on 26.01.2017.
 */

public class MyRunner extends AbstractRunner<Application>
{

    @Override
    protected String getApplicationClassName()
    {
        return ModuleProvider.class.getName();
    }
}
