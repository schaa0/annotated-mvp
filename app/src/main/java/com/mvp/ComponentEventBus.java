package com.mvp;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;

public interface ComponentEventBus {
    EventBus eventBus();
    Context context();
    LocationManager locationManager();
    SensorManager sensorManager();
}
