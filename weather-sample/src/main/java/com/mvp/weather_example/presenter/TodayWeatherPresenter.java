package com.mvp.weather_example.presenter;

import android.location.Location;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ComponentWeather;
import com.mvp.weather_example.model.Weather;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Andy on 22.12.2016.
 */

@Presenter(needsComponents = {ComponentWeather.class})
public class TodayWeatherPresenter extends WeatherPresenter
{

    protected TodayWeatherPresenter()
    {
    }

    @Inject
    public TodayWeatherPresenter(LocationProvider locationProvider, WeatherService weatherService, @Named("Today") WeatherResponseFilter weatherParser)
    {
        super(locationProvider, weatherService, weatherParser);
    }

    @Override
    protected void loadWeather(double longitude, double latitude)
    {
        try
        {
            dispatchRequestStarted();
            final Weather weather = weatherService.getCurrentWeather(longitude, latitude, "metric");
            updateState(weather);
            submitOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    getView().showWeather(lastTemperature, lastHumidity);
                    weatherService.loadIcon(weather.icon(), TodayWeatherPresenter.this);
                    dispatchRequestFinished();
                }
            });
        } catch (IOException e)
        {
            dispatchEvent(e).toAny();
        }
    }

    @BackgroundThread
    public void loadForecastWeatherDataForToday()
    {
        try
        {
            Location lastKnownLocation = locationProvider.lastLocation();
            if (lastKnownLocation != null)
            {
                double longitude = lastKnownLocation.getLongitude();
                double latitude = lastKnownLocation.getLatitude();
                dispatchRequestStarted();
                ThreeHoursForecastWeather weather =
                        weatherService.getForecastWeather(longitude, latitude, "metric");
                dispatchRequestFinished();
                final String forecastData = weatherParser.parse(weather);
                submitOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getView().showForecastWeather(forecastData);
                    }
                });
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
