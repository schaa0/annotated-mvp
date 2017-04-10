package com.mvp.weather_example.service;

import com.mvp.annotation.ApplicationScope;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
import com.mvp.weather_example.model.today.TodayWeather;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class WeatherService
{

    private final WeatherApi api;
    private final ImageRequestManager imageRequestManager;
    private String apiKey;

    @Inject
    public WeatherService(WeatherApi api, ImageRequestManager imageRequestManager, @Named("apiKey") String apiKey){
        this.api = api;
        this.imageRequestManager = imageRequestManager;
        this.apiKey = apiKey;
    }


    public void loadIcon(String icon, final ImageRequestManager.IconCallback iconCallback) {
        imageRequestManager.load(icon, iconCallback);
    }

    public TomorrowWeather getTomorrowWeather(double longitude, double latitude, int forecastDays) throws IOException
    {
        Call<TomorrowWeather> call = api.getTomorrowWeather(longitude, latitude, "metric", forecastDays, "de", apiKey);
        Response<TomorrowWeather> execute = call.execute();
        TomorrowWeather tomorrowWeather = execute.body();
        return tomorrowWeather;
    }

    public ThreeHoursForecastWeather getForecastWeather(double longitude, double latitude) throws IOException
    {
        Call<ThreeHoursForecastWeather> call = api.getForecastWeather(longitude, latitude, "metric", "de", apiKey);
        Response<ThreeHoursForecastWeather> execute = call.execute();
        ThreeHoursForecastWeather threeHoursForecastWeather = execute.body();
        return threeHoursForecastWeather;
    }

    public TodayWeather getCurrentWeather(double longitude, double latitude) throws IOException
    {
        Call<TodayWeather> call = api.getCurrentWeather(longitude, latitude, "metric", "de", apiKey);
        Response<TodayWeather> response = call.execute();
        TodayWeather todayWeather = response.body();
        return todayWeather;
    }

}
