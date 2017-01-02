package com.mvp.weather_example.view;

import com.mvp.MvpView;

/**
 * Created by Andy on 23.12.2016.
 */
public interface IThreeHourForecastView extends MvpView {
    void showThreeHourForecast(String forecastWeather);
}
