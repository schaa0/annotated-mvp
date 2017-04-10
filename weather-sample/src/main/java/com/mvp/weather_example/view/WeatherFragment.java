package com.mvp.weather_example.view;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mvp.weather_example.R;
import com.mvp.weather_example.presenter.WeatherPresenter;

public abstract class WeatherFragment extends Fragment implements WeatherFragmentView
{

    public TextView temperatureTextView;
    TextView humidityTextView;
    ImageView imageView;
    ProgressBar progressBar;
    TextView cityTextView;
    TextView descTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_weather_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        descTextView = (TextView) getView().findViewById(R.id.descriptionTextView);
        cityTextView = (TextView) getView().findViewById(R.id.cityTextView);
        temperatureTextView = (TextView) getView().findViewById(R.id.temperatureTextView);
        humidityTextView = (TextView) getView().findViewById(R.id.humidityTextView);
        imageView = (ImageView) getView().findViewById(R.id.imageView);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        imageView.setOnClickListener((view) -> onWeatherIconClicked());
    }

    protected abstract void onWeatherIconClicked();

    @Override
    public void showWeather(String city, String description, String temperature, String humidity) {
        descTextView.setText(description);
        cityTextView.setText(getString(R.string.city_format, city));
        temperatureTextView.setText(getString(R.string.temperature_format, temperature));
        humidityTextView.setText(getString(R.string.humidity_format, humidity));
    }

    @Override
    public boolean isPermissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void requestPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, requestCode);
        }else{
            getPresenter().onPermissionsResult(requestCode, new String[]{permission}, new int[] {PackageManager.PERMISSION_GRANTED});
        }
    }

    protected abstract WeatherPresenter getPresenter();

    @Override
    public void showIcon(Bitmap icon) {
        imageView.setImageBitmap(icon);
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
