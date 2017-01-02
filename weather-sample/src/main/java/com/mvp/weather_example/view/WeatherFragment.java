package com.mvp.weather_example.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mvp.weather_example.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public abstract class WeatherFragment extends Fragment implements IWeatherView{

    @BindView(R.id.temperatureTextView) TextView temperatureTextView;
    @BindView(R.id.humidityTextView) TextView humidityTextView;
    @BindView(R.id.imageView) ImageView imageView;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, getView());
    }

    @OnClick(R.id.imageView)
    protected abstract void onWeatherIconClicked();

    @Override
    public int provideCurrentOrientation() {
        return getActivity().getResources().getConfiguration().orientation;
    }

    @Override
    public void showWeather(String temperature, String humidity) {
        temperatureTextView.setText(temperature);
        humidityTextView.setText(humidity);
    }

    @Override
    public boolean isPermissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
    }

    @Override
    public void showIcon(Bitmap icon) {
        imageView.setImageBitmap(icon);
    }

    @Override
    public void showForecastWeather(String forecastString) {
        Intent intent = new Intent(getActivity(), ThreeHourForecastActivity.class);
        intent.putExtra(ThreeHourForecastActivity.KEY_FORECAST, forecastString);
        startActivity(intent);
    }

    @Override
    public void requestStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void requestFinished() {
        progressBar.setVisibility(View.GONE);
    }

}
