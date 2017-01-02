package com.mvp.weather_example.view;

import android.support.annotation.NonNull;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;
import com.mvp.weather_example.presenter.TomorrowWeatherPresenter;
import com.mvp.weather_example.presenter.WeatherPresenter;

@UIView(presenter = TomorrowWeatherPresenter.class)
public class TomorrowWeatherFragment extends WeatherFragment {

    @Presenter TomorrowWeatherPresenter presenter;

    @Override
    protected void onWeatherIconClicked() {
        presenter.loadForecastWeatherDataForTomorrow();
    }

    @Override
    protected WeatherPresenter getPresenter() {
        return presenter;
    }
}
