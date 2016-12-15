package de.hda.simple_example.inject;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 05.12.2016.
 */

@Module
public class ModuleLocationManager {

    private Context context;

    public ModuleLocationManager(Context context){
        this.context = context;
    }

    public ModuleLocationManager() { }

    @Provides
    @Named("location")
    public LocationManager locationManager(){
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    public SensorManager sensorManager() {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

}
