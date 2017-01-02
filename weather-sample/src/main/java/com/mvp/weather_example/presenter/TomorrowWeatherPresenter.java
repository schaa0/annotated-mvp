package com.mvp.weather_example.presenter;

import android.location.Location;
import android.location.LocationManager;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ComponentWeather;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
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
public class TomorrowWeatherPresenter extends WeatherPresenter {

    private static final int FORECAST_DAYS = 1;

    protected TomorrowWeatherPresenter() { }

    @Inject
    public TomorrowWeatherPresenter(LocationManager locationManager, WeatherService weatherService, ImageRequestManager requestManager, DateProvider dateProvider) {
        super(locationManager, weatherService, requestManager, dateProvider);
    }

    @Override
    protected boolean isCorrectDay(Calendar currentDate, Calendar parsedDate) {
        if (currentDate.get(Calendar.YEAR) != parsedDate.get(Calendar.YEAR)) return false;
        if (currentDate.get(Calendar.MONTH) != parsedDate.get(Calendar.MONTH)) return false;

        Calendar clone = Calendar.getInstance();
        clone.setTimeZone(currentDate.getTimeZone());
        clone.setTime(currentDate.getTime());
        clone.add(Calendar.DAY_OF_YEAR, 1);

        if (clone.get(Calendar.DAY_OF_YEAR) != parsedDate.get(Calendar.DAY_OF_YEAR)) return false;
        return true;
    }

    @Override
    public void loadWeather(double longitude, double latitude) {
        Call<TomorrowWeather> call = weatherService.getTomorrowWeather(longitude, latitude, "metric", FORECAST_DAYS, WeatherService.API_KEY);
        internalShowWeather(call);
    }

    @BackgroundThread
    public void loadForecastWeatherDataForTomorrow() {
        Location lastKnownLocation = getLastKnownLocation();
        if (lastKnownLocation != null) {
            double longitude = lastKnownLocation.getLongitude();
            double latitude = lastKnownLocation.getLatitude();
            Call<ThreeHoursForecastWeather> call = weatherService.getForecastWeather(longitude, latitude, "metric", WeatherService.API_KEY);
            Calendar currentDate = dateProvider.getCurrentDate();
            currentDate.set(Calendar.HOUR_OF_DAY, 23);
            currentDate.set(Calendar.MINUTE, 59);
            currentDate.set(Calendar.SECOND, 59);
            currentDate.set(Calendar.MILLISECOND, 0);
            internalShowForecastWeather(call, currentDate);
        }
    }
}
