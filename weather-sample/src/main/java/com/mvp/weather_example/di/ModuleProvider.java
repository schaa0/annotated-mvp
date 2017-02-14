package com.mvp.weather_example.di;

import android.support.v7.app.AppCompatActivity;

import com.mvp.BaseApplicationProvider;
import com.mvp.annotation.Provider;
import com.mvp.annotation.ProvidesComponent;
import com.mvp.annotation.ProvidesModule;

@Provider
public class ModuleProvider extends BaseApplicationProvider {

    private ComponentWeather componentWeather;

    @Override
    public void onCreate() {
        super.onCreate();
        componentWeather = DaggerComponentWeather.builder()
                                                 .moduleWeather(this.moduleWeather())
                                                 .componentEventBus(this.componentEventBus())
                                                 .build();
    }

    @ProvidesModule
    public ModuleWeather moduleWeather(){
        return new ModuleWeather(this.getApplicationContext());
    }

    @ProvidesComponent
    public ComponentWeather componentWeather(){
        return componentWeather;
    }

    @ProvidesModule
    public ModuleThreeHourForecast getThreeHourForecast(String threeHourForecastWeather){
        return new ModuleThreeHourForecast(threeHourForecastWeather);
    }

    @ProvidesModule
    public ModuleViewPagerFragmentFactory moduleFragmentFactory(AppCompatActivity activity){
        return new ModuleViewPagerFragmentFactory(activity);
    }

    @ProvidesComponent
    public ComponentActivity createComponentActivity(AppCompatActivity activity)
    {
        return DaggerComponentActivity.builder()
                               .componentEventBus(this.componentEventBus())
                               .moduleViewPagerFragmentFactory(this.moduleFragmentFactory(activity))
                               .build();
    }
}
