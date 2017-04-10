package com.mvp.weather_example.service;

import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
import com.mvp.weather_example.model.today.TodayWeather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi
{

    @GET("/data/2.5/weather")
    Call<TodayWeather> getCurrentWeather(@Query("lon") double longitude, @Query("lat")double latitude, @Query("units") String metric, @Query("lang") String lang, @Query("appid") String apiKey);

    @GET("/data/2.5/forecast/daily")
    Call<TomorrowWeather> getTomorrowWeather(@Query("lon") double longitude, @Query("lat") double latitude, @Query("units") String metric, @Query("cnt") int days, @Query("lang") String lang, @Query("appid") String apiKey);

    @GET("/data/2.5/forecast")
    Call<ThreeHoursForecastWeather> getForecastWeather(@Query("lon") double longitude, @Query("lat") double latitude, @Query("units") String metric, @Query("lang") String lang, @Query("appid") String apiKey);
}
