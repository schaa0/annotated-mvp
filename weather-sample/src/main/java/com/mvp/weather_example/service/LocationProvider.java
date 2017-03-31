package com.mvp.weather_example.service;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.mvp.annotation.ApplicationScope;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocationProvider implements LocationListener
{

    public static final int MIN_LOCATION_UPDATE_INTERVAL = 5000;
    public static final int MIN_DISTANCE_IN_METERS = 100;

    private LocationManager locationManager;
    private List<OnLocationChangedListener> locationListeners = new ArrayList<>();

    public void addOnLocationChangedListener(OnLocationChangedListener locationListener)
    {
        if (!locationListeners.contains(locationListener))
            locationListeners.add(locationListener);
    }

    public void removeOnLocationChangedListener(OnLocationChangedListener locationListener){
        if (locationListeners.contains(locationListener))
            locationListeners.remove(locationListener);
    }

    @Inject
    public LocationProvider(LocationManager locationManager)
    {
        this.locationManager = locationManager;
    }

    public Location lastLocation()
    {
        return getLastKnownLocation(getBestProvider());
    }

    private Location getLastKnownLocation(String bestProvider)
    {
        try
        {
            return locationManager.getLastKnownLocation(bestProvider);
        } catch (SecurityException e)
        {

        }
        return null;
    }

    private String getBestProvider()
    {
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        return locationManager.getBestProvider(criteria, false);
    }

    public void requestLocationUpdates()
    {
        locationManager.requestLocationUpdates(getBestProvider(), MIN_LOCATION_UPDATE_INTERVAL, MIN_DISTANCE_IN_METERS, this);
    }

    public void removeUpdates()
    {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location)
    {
        for (OnLocationChangedListener locationListener : locationListeners)
            locationListener.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {
        requestLocationUpdates();
        onLocationChanged(lastLocation());
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        locationManager.removeUpdates(this);
    }

    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);
    }

}
