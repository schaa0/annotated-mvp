package com.mvp.weather_example.di;

import com.mvp.ModuleActivity;
import com.mvp.annotation.ActivityScope;
import com.mvp.weather_example.view.MainActivity;

import dagger.Subcomponent;

@Subcomponent(modules = {ModuleActivity.class})
@ActivityScope
public interface ComponentActivity {
    void inject(MainActivity activity);
}
