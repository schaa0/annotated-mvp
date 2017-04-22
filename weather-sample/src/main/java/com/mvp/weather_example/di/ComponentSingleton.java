package com.mvp.weather_example.di;

import android.content.SharedPreferences;

import com.mvp.ComponentEventBus;
import com.mvp.BaseModuleContext;
import com.mvp.ModuleEventBus;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.filter.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.filter.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = { ModuleSingleton.class, BaseModuleContext.class, ModuleEventBus.class})
@Singleton
public interface ComponentSingleton extends ComponentEventBus
{
    SharedPreferences sharedPreferences();
    LocationProvider locationProvider();
    WeatherService weatherService();
    TodayWeatherResponseFilter todayWeatherParser();
    TomorrowWeatherResponseFilter tomorrowWeatherParser();
}
