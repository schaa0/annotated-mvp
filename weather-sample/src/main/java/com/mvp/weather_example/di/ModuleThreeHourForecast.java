package com.mvp.weather_example.di;

import com.mvp.ModuleEventBus;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 23.12.2016.
 */

@Module
public class ModuleThreeHourForecast {

    private String forecastWeather;

    public ModuleThreeHourForecast(String forecastWeather){
        this.forecastWeather = forecastWeather;
    }

    @Provides
    public String forecastWeather(){
        return forecastWeather;
    }

}
