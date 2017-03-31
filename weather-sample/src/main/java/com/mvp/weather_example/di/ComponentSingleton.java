package com.mvp.weather_example.di;

import com.mvp.ComponentEventBus;
import com.mvp.EventBus;
import com.mvp.ModuleActivity;
import com.mvp.ModuleContext;
import com.mvp.ModuleEventBus;
import com.mvp.Router;
import com.mvp.annotation.ApplicationScope;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.filter.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.filter.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = { ModuleSingleton.class, ModuleContext.class, ModuleEventBus.class})
@Singleton
public interface ComponentSingleton extends ComponentEventBus
{
    LocationProvider locationProvider();
    WeatherService weatherService();
    TodayWeatherResponseFilter todayWeatherParser();
    TomorrowWeatherResponseFilter tomorrowWeatherParser();
    ComponentActivity plus(ModuleActivity module);
}
