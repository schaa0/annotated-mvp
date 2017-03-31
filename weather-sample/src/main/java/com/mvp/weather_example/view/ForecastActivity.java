package com.mvp.weather_example.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.mvp.annotation.ModuleParam;
import com.mvp.annotation.Presenter;
import com.mvp.annotation.View;
import com.mvp.weather_example.R;
import com.mvp.weather_example.presenter.ForecastPresenter;

@View(presenter = ForecastPresenter.class)
public class ForecastActivity extends AppCompatActivity implements ForecastActivityView, DialogInterface.OnClickListener{

    public static final String KEY_FORECAST = "forecast";

    @Presenter
    ForecastPresenter presenter;

    @ModuleParam
    public String forecastWeather() {
        return getIntent().getStringExtra(KEY_FORECAST);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_three_hour_forecast_activity);
    }

    @Override
    public void showThreeHourForecast(String forecastWeather) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Three Hour Forecast")
                .setMessage(forecastWeather)
                .setPositiveButton("OK", this)
                .show();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        onBackPressed();
    }
}
