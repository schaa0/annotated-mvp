package com.mvp.weather_example.di;

import com.mvp.BaseComponentActivity;
import com.mvp.BaseModuleActivity;
import com.mvp.annotation.ActivityScope;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.ViewPagerFragmentFactory;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.service.filter.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.filter.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.view.MainActivity;

import dagger.Component;

@Component(modules = { ModuleActivity.class }, dependencies = { ComponentSingleton.class })
@ActivityScope
public interface ComponentActivity extends BaseComponentActivity {
    LocationProvider locationProvider();
    WeatherService weatherService();
    ViewPagerFragmentFactory viewPagerFragmentFactory();
    TodayWeatherResponseFilter todayWeatherParser();
    TomorrowWeatherResponseFilter tomorrowWeatherParser();
    void inject(MainActivity activity);
}
