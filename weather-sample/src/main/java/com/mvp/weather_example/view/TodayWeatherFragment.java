package com.mvp.weather_example.view;

import android.support.annotation.NonNull;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;
import com.mvp.weather_example.presenter.WeatherPresenter;

/**
 * Created by Andy on 22.12.2016.
 */

@UIView(presenter = TodayWeatherPresenter.class)
public class TodayWeatherFragment extends WeatherFragment {

    @Presenter TodayWeatherPresenter presenter;

    @Override
    protected void onWeatherIconClicked() {
        presenter.loadForecastWeatherDataForToday();
    }

    @Override
    protected WeatherPresenter getPresenter() {
        return presenter;
    }
}
