package com.mvp.weather_example.di;

import android.location.LocationManager;

import com.mvp.ComponentEventBus;
import com.mvp.ModuleEventBus;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import javax.inject.Named;

import dagger.Component;

/**
 * Created by Andy on 25.12.2016.
 */

@Component(modules = { ModuleWeather.class, ModuleEventBus.class}, dependencies = {ComponentEventBus.class})
@ApplicationScope
public interface ComponentWeather {
    LocationManager locationManager();
    WeatherService weatherService();
    ImageRequestManager imageRequestManager();
    @Named("Today")
    WeatherResponseFilter todayWeatherParser();
    @Named("Tomorrow")
    WeatherResponseFilter tomorrowWeatherParser();
}
