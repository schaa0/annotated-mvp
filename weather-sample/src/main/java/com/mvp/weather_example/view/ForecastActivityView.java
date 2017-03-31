package com.mvp.weather_example.view;

import com.mvp.MvpView;

public interface ForecastActivityView extends MvpView {
    void showThreeHourForecast(String forecastWeather);
}
