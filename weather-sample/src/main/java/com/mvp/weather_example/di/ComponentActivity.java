package com.mvp.weather_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.weather_example.view.MainActivity;

import dagger.Component;

@Component(dependencies = {ComponentEventBus.class}, modules = {ModuleViewPagerFragmentFactory.class})
@ActivityScope
public interface ComponentActivity {
    void inject(MainActivity activity);
}
