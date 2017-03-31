package com.mvp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Andy on 30.03.2017.
 */
@Module
public class ModuleContext
{
    private Context context;

    public ModuleContext(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context context() {
        return this.context;
    }

    @Provides
    @Singleton
    public LocationManager locationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Provides
    @Singleton
    public SensorManager sensorManager(Context context) {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Provides
    @Singleton
    public SharedPreferences sharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
