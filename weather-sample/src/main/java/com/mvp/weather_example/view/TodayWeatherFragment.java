package com.mvp.weather_example.view;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;
import com.mvp.weather_example.presenter.WeatherPresenter;

@View(presenter = TodayWeatherPresenter.class)
public class TodayWeatherFragment extends WeatherFragment
{

    @Presenter
    TodayWeatherPresenter presenter;

    @Override
    protected void onWeatherIconClicked()
    {
        presenter.loadForecastWeatherDataForToday();
    }

    @Override
    protected WeatherPresenter getPresenter()
    {
        return presenter;
    }
}
