package com.mvp.weather_example.presenter;

import android.location.Location;

import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ComponentSingleton;
import com.mvp.weather_example.model.Weather;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.filter.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.ThreeHourForecastActivity;

import java.io.IOException;

import javax.inject.Inject;

@Presenter(needsComponents = {ComponentSingleton.class})
public class TodayWeatherPresenter extends WeatherPresenter
{

    protected TodayWeatherPresenter()
    {
    }

    @Inject
    public TodayWeatherPresenter(LocationProvider locationProvider, WeatherService weatherService, TodayWeatherResponseFilter weatherParser)
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
            submitOnUiThread(() -> {
                getView().showWeather(lastTemperature, lastHumidity);
                weatherService.loadIcon(weather.icon(), TodayWeatherPresenter.this);
                dispatchRequestFinished();
            });
        } catch (IOException e)
        {
            dispatchEvent(e).toAny();
            dispatchRequestFinished();
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
                submitOnUiThread(() -> navigateToDetailScreen(forecastData));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            dispatchRequestFinished();
        }
    }

    private void navigateToDetailScreen(String forecast)
    {
        getRouter()
                .navigateTo(ThreeHourForecastActivity.class)
                .putExtra(ThreeHourForecastActivity.KEY_FORECAST, forecast)
                .open();
    }
}
