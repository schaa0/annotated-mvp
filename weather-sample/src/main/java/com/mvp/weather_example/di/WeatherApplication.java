package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseModuleActivity;
import com.mvp.MvpApplication;
import com.mvp.ModuleContext;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

@Provider
public class WeatherApplication extends MvpApplication {

    private ComponentSingleton componentWeather;

    @Override
    public void onCreate() {
        super.onCreate();
        componentWeather = DaggerComponentSingleton.builder()
                                                 .moduleSingleton(new ModuleSingleton())
                                                 .moduleContext(new ModuleContext(this.getApplicationContext()))
                                                 .moduleEventBus(this.mvpEventBus())
                                                 .build();
    }

    @ProvidesComponent
    public ComponentSingleton componentSingleton(){
        return componentWeather;
    }

    @ProvidesComponent
    public ComponentActivity createComponentActivity(AppCompatActivity activity)
    {
        return DaggerComponentActivity.builder()
                .componentSingleton(this.componentSingleton())
                .moduleActivity(new ModuleActivity(activity))
                .build();
    }
}
