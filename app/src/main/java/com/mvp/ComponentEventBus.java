package com.mvp;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;

import javax.inject.Singleton;

import dagger.Component;

public interface ComponentEventBus {
    EventBus eventBus();
    Context context();
    Router router();
    LocationManager locationManager();
    SensorManager sensorManager();
}
