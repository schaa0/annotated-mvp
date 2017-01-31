package com.mvp.weather_example.di;

import android.content.Context;
import android.location.LocationManager;

import com.bumptech.glide.Glide;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.ImageRequestManagerImpl;
import com.mvp.weather_example.service.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleWeather {

    private final Context context;

    public ModuleWeather(Context context){
        this.context = context.getApplicationContext();
    }

    @Provides
    @ApplicationScope
    public LocationManager locationManager() {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @ApplicationScope
    public WeatherService weatherService() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://api.openweathermap.org")
                .build()
                .create(WeatherService.class);
    }

    @Provides
    @ApplicationScope
    public ImageRequestManager glide(){
        return new ImageRequestManagerImpl(Glide.with(context));
    }

    /*@Provides
    @ApplicationScope
    public DateProvider dateProvider() {
        return new DateProvider();
    }*/

    @Provides
    @Named("Today")
    @ApplicationScope
    public WeatherResponseFilter todayWeatherParser(DateProvider dateProvider) {
        return new TodayWeatherResponseFilter(dateProvider);
    }

    @Provides
    @Named("Tomorrow")
    @ApplicationScope
    public WeatherResponseFilter tomorrowWeatherParser(DateProvider dateProvider) {
        return new TomorrowWeatherResponseFilter(dateProvider);
    }
}
