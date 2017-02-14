package com.mvp.weather_example.di;

import dagger.Module;
import dagger.Provides;

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
