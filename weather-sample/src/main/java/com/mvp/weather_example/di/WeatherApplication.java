package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseApplicationProvider;
import com.mvp.ModuleActivity;
import com.mvp.ModuleContext;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

@Provider
public class WeatherApplication extends BaseApplicationProvider {

    private ComponentSingleton componentWeather;

    @Override
    public void onCreate() {
        super.onCreate();
        componentWeather = DaggerComponentSingleton.builder()
                                                 .moduleSingleton(new ModuleSingleton())
                                                 .moduleContext(new ModuleContext(this.getApplicationContext()))
                                                 .moduleEventBus(this.mvpEventBus())
                                                 .build();
    }

    @ProvidesModule
    public ModuleThreeHourForecast getThreeHourForecast(String threeHourForecastWeather){
        return new ModuleThreeHourForecast(threeHourForecastWeather);
    }

    @ProvidesComponent
    public ComponentSingleton componentSingleton(){
        return componentWeather;
    }

    public ComponentActivity createComponentActivity(AppCompatActivity activity)
    {
        return componentWeather.plus(new ModuleActivity(activity));
    }
}
