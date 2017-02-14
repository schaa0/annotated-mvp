package com.mvp.weather_example.di;

import android.content.Context;
import android.location.LocationManager;

import com.bumptech.glide.Glide;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.ImageRequestManagerImpl;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.service.WeatherApi;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import java.util.concurrent.TimeUnit;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ModuleWeather {

    private static final String API_KEY = "52e258b9d648c55104d57055ee214f5a";
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
    public LocationProvider locationProvider(LocationManager locationManager) {
        return new LocationProvider(locationManager);
    }

    @Provides
    @ApplicationScope
    public WeatherService weatherService(WeatherApi weatherApi, ImageRequestManager imageRequestManager) {
        return new WeatherService(weatherApi, imageRequestManager, API_KEY);
    }

    @Provides
    @ApplicationScope
    public WeatherApi weatherAPI() {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://api.openweathermap.org")
                .client(new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build())
                .build()
                .create(WeatherApi.class);
    }

    @Provides
    @ApplicationScope
    public ImageRequestManager glide(){
        return new ImageRequestManagerImpl(Glide.with(context));
    }

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
