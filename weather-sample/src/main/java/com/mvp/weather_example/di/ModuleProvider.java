package com.mvp.weather_example.di;

import com.mvp.BaseApplicationProvider;
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
        componentWeather = DaggerComponentWeather.builder()
                .moduleWeather(new ModuleWeather(this.getApplicationContext()))
                .componentEventBus(this.componentEventBus())
                .build();
    }

    @ProvidesComponent
    public ComponentWeather componentWeather(){
        return componentWeather;
    }

    @ProvidesModule
    public ModuleThreeHourForecast getThreeHourForecast(String threeHourForecastWeather){
        return new ModuleThreeHourForecast(threeHourForecastWeather);
    }

}
