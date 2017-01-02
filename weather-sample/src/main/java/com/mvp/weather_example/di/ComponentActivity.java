package com.mvp.weather_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.weather_example.view.MainActivity;

import dagger.Component;

/**
 * Created by Andy on 02.01.2017.
 */

@Component(dependencies = {ComponentEventBus.class})
@PerActivity
public interface ComponentActivity {
    void inject(MainActivity activity);
}
