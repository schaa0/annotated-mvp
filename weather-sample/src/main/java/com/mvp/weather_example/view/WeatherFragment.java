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


public abstract class WeatherFragment extends Fragment implements IWeatherView{

    TextView temperatureTextView;
    TextView humidityTextView;
    ImageView imageView;
    ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        temperatureTextView = (TextView) getView().findViewById(R.id.temperatureTextView);
        humidityTextView = (TextView) getView().findViewById(R.id.humidityTextView);
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWeatherIconClicked();
            }
        });
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

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
        intent.putExtra("forecast", forecastString);
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


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
