package com.mvp.weather_example.service;

import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
import com.mvp.weather_example.model.today.TodayWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Andy on 22.12.2016.
 */

public interface WeatherService {

    String API_KEY = "52e258b9d648c55104d57055ee214f5a";

    @GET("/data/2.5/weather")
    Call<TodayWeather> getCurrentWeather(@Query("lon") double longitude, @Query("lat")double latitude, @Query("units") String metric, @Query("appid") String apiKey);

    @GET("/data/2.5/forecast/daily")
    Call<TomorrowWeather> getTomorrowWeather(@Query("lon") double longitude, @Query("lat") double latitude, @Query("units") String metric, @Query("cnt") int days, @Query("appid") String apiKey);

    @GET("/data/2.5/forecast")
    Call<ThreeHoursForecastWeather> getForecastWeather(@Query("lon") double longitude, @Query("lat") double latitude, @Query("units") String metric, @Query("appid") String apiKey);
}
