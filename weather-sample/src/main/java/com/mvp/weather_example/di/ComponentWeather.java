package com.mvp.weather_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.ModuleEventBus;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.service.WeatherResponseFilter;

import javax.inject.Named;

import dagger.Component;

/**
 * Created by Andy on 25.12.2016.
 */

@Component(modules = { ModuleWeather.class, ModuleEventBus.class}, dependencies = {ComponentEventBus.class})
@ApplicationScope
public interface ComponentWeather {
    LocationProvider locationProvider();
    WeatherService weatherService();
    @Named("Today") WeatherResponseFilter todayWeatherParser();
    @Named("Tomorrow") WeatherResponseFilter tomorrowWeatherParser();
}
