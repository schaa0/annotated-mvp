package com.mvp.weather_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.ModuleEventBus;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import javax.inject.Named;

import dagger.Component;

@Component(modules = { ModuleWeather.class, ModuleEventBus.class}, dependencies = {ComponentEventBus.class})
@ApplicationScope
public interface ComponentWeather {
    LocationProvider locationProvider();
    WeatherService weatherService();
    @Named("Today") WeatherResponseFilter todayWeatherParser();
    @Named("Tomorrow") WeatherResponseFilter tomorrowWeatherParser();
}
