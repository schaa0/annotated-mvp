package de.hda.simple_example;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;

import de.hda.simple_example.inject.ModuleLocationManager;

import static org.mockito.Mockito.mock;

/**
 * Created by Andy on 11.12.2016.
 */
public class ModuleMockLocationManager extends ModuleLocationManager {
    public ModuleMockLocationManager() {
        super(null);
    }

    @Override
    public LocationManager locationManager() {
        return mock(LocationManager.class);
    }

    @Override
    public SensorManager sensorManager() {
        return mock(SensorManager.class);
    }
}
