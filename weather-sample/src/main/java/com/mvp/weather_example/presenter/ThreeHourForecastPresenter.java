package com.mvp.weather_example.presenter;

import com.mvp.MvpPresenter;
import com.mvp.annotation.Presenter;
import com.mvp.weather_example.di.ModuleThreeHourForecast;
import com.mvp.weather_example.view.IThreeHourForecastView;

import javax.inject.Inject;

@Presenter(needsModules = ModuleThreeHourForecast.class)
public class ThreeHourForecastPresenter extends MvpPresenter<IThreeHourForecastView> {

    private String forecastWeather;

    protected ThreeHourForecastPresenter() { }

    @Inject
    public ThreeHourForecastPresenter(String forecastWeather){
        this.forecastWeather = forecastWeather;
    }

    @Override
    public void onViewAttached(IThreeHourForecastView view) {
        view.showThreeHourForecast(forecastWeather);
    }

    @Override
    public void onViewReattached(IThreeHourForecastView view) {
        view.showThreeHourForecast(forecastWeather);
    }

    @Override
    public void onViewDetached(IThreeHourForecastView view) {

    }
}
