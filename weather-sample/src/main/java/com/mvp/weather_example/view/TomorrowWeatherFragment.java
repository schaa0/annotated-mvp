package com.mvp.weather_example.view;

import android.support.annotation.NonNull;

import com.mvp.annotation.Presenter;
import com.mvp.annotation.UIView;
import com.mvp.weather_example.presenter.TomorrowWeatherPresenter;

@UIView(presenter = TomorrowWeatherPresenter.class)
public class TomorrowWeatherFragment extends WeatherFragment {

    @Presenter TomorrowWeatherPresenter presenter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        presenter.onPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onWeatherIconClicked() {
        presenter.loadForecastWeatherDataForTomorrow();
    }
}
