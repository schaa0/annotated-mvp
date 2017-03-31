package com.mvp.weather_example.view;

import android.graphics.Bitmap;

import com.mvp.MvpView;

public interface WeatherView extends MvpView {
    void requestStarted();
    void requestFinished();
    void showWeather(String temperature, String humidity);
    boolean isPermissionGranted(String permission);
    void requestPermission(String permission, int requestCode);
    void showIcon(Bitmap icon);
}
