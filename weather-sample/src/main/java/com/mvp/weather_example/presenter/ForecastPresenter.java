package com.mvp.weather_example.presenter;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ComponentActivity;
import com.mvp.weather_example.view.ForecastActivityView;

import javax.inject.Inject;

@Presenter(needsComponents = ComponentActivity.class)
public class ForecastPresenter extends MvpPresenter<ForecastActivityView> {

    private String forecastWeather;

    protected ForecastPresenter() { }

    @Inject
    public ForecastPresenter(String forecastWeather){
        this.forecastWeather = forecastWeather;
    }

    @Override
    public void onViewAttached(ForecastActivityView view) {
        view.showThreeHourForecast(forecastWeather);
    }

    @Override
    public void onViewReattached(ForecastActivityView view) {
        view.showThreeHourForecast(forecastWeather);
    }

    @Override
    public void onViewDetached(ForecastActivityView view) {

    }
}
