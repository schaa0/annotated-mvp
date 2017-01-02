package com.mvp.weather_example.presenter;

import android.location.Location;
import android.location.LocationManager;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ComponentWeather;
import com.mvp.weather_example.event.PermissionEvent;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.today.TodayWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.WeatherService;

import java.util.Calendar;

import javax.inject.Inject;

import retrofit2.Call;

/**
 * Created by Andy on 22.12.2016.
 */

@Presenter(needsComponents = { ComponentWeather.class })
public class TodayWeatherPresenter extends WeatherPresenter {

    protected TodayWeatherPresenter() { }

    @Inject
    public TodayWeatherPresenter(LocationManager locationManager, WeatherService weatherService, ImageRequestManager requestManager, DateProvider dateProvider) {
        super(locationManager, weatherService, requestManager, dateProvider);
    }

    @Override
    protected boolean isCorrectDay(Calendar currentDate, Calendar parsedDate) {
        if (currentDate.get(Calendar.YEAR) != parsedDate.get(Calendar.YEAR)) return false;
        if (currentDate.get(Calendar.MONTH) != parsedDate.get(Calendar.MONTH)) return false;
        if (currentDate.get(Calendar.DAY_OF_YEAR) != parsedDate.get(Calendar.DAY_OF_YEAR)) return false;
        return true;
    }

    @Override
    public void loadWeather(double longitude, double latitude) {
        Call<TodayWeather> call = weatherService.getCurrentWeather(longitude, latitude, "metric", WeatherService.API_KEY);
        internalShowWeather(call);
    }

    @BackgroundThread
    public void loadForecastWeatherDataForToday() {
        Location lastKnownLocation = getLastKnownLocation();
        if (lastKnownLocation != null) {
            double longitude = lastKnownLocation.getLongitude();
            double latitude = lastKnownLocation.getLatitude();
            Call<ThreeHoursForecastWeather> call = weatherService.getForecastWeather(longitude, latitude, "metric", WeatherService.API_KEY);
            internalShowForecastWeather(call, dateProvider.getCurrentDate());
        }
    }

}
