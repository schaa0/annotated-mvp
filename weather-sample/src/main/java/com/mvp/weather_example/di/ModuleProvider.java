package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseApplicationProvider;
import com.mvp.ComponentEventBus;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

/**
 * Created by Andy on 22.12.2016.
 */

@Provider
public class ModuleProvider extends BaseApplicationProvider {

    private ComponentWeather componentWeather;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @ProvidesModule
    public ModuleWeather moduleWeather(){
        return new ModuleWeather(this.getApplicationContext());
    }

    @ProvidesComponent
    public ComponentWeather componentWeather(ModuleWeather moduleWeather, ComponentEventBus componentEventBus){
        if (componentWeather == null){
            componentWeather = DaggerComponentWeather.builder()
                    .moduleWeather(moduleWeather)
                    .componentEventBus(componentEventBus)
                    .build();
        }
        return componentWeather;
    }

    @ProvidesModule
    public ModuleThreeHourForecast getThreeHourForecast(String threeHourForecastWeather){
        return new ModuleThreeHourForecast(threeHourForecastWeather);
    }

    @ProvidesModule
    public ModuleFragmentFactory moduleFragmentFactory(AppCompatActivity activity){
        return new ModuleFragmentFactory(activity);
    }
}
