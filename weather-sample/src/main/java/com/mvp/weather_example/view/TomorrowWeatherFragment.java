package com.mvp.weather_example.view;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;
import com.mvp.weather_example.presenter.TomorrowWeatherPresenter;
import com.mvp.weather_example.presenter.WeatherPresenter;

@View(presenter = TomorrowWeatherPresenter.class)
public class TomorrowWeatherFragment extends WeatherFragment {

    public @Presenter TomorrowWeatherPresenter presenter;

    @Override
    protected void onWeatherIconClicked() {
        presenter.loadForecastWeatherDataForTomorrow();
    }

    @Override
    protected WeatherPresenter getPresenter() {
        return presenter;
    }
}
