package com.mvp.weather_example.presenter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Location;

import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.weather_example.event.PermissionEvent;
import com.mvp.weather_example.model.Weather;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.filter.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.WeatherFragmentView;

public abstract class WeatherPresenter extends MvpPresenter<WeatherFragmentView> implements LocationProvider.OnLocationChangedListener, ImageRequestManager.IconCallback
{

    public static final int REQUEST_CODE_PERM_ACCESS_FINE_LOCATION = 45;
    public static final int REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION = 23;

    protected LocationProvider locationProvider;
    protected WeatherService weatherService;
    protected WeatherResponseFilter weatherParser;

    protected String lastTemperature;
    protected String lastHumidity;
    protected String lastCity;
    protected String lastDescription;

    private Bitmap icon;
    private Location lastLocation = null;

    protected WeatherPresenter()
    {
    }

    public WeatherPresenter(LocationProvider locationProvider, WeatherService weatherService, WeatherResponseFilter weatherParser)
    {
        this.locationProvider = locationProvider;
        this.weatherService = weatherService;
        this.weatherParser = weatherParser;
    }

    @BackgroundThread
    protected abstract void loadWeather(double longitude, double latitude);

    @Override
    public void onViewAttached(WeatherFragmentView view)
    {
        requestPermissionsIfNeeded(view);
        locationProvider.addOnLocationChangedListener(this);
        if (hasAllPermissions())
        {
            locationProvider.requestLocationUpdates();
            loadWeatherIfAllPermissionsGranted(view);
        }
    }

    @Override
    public void onViewReattached(WeatherFragmentView view)
    {
        requestPermissionsIfNeeded(view);
        locationProvider.addOnLocationChangedListener(this);
        if (hasAllPermissions())
        {
            locationProvider.requestLocationUpdates();
        }
        if (lastTemperature != null && lastHumidity != null)
        {
            view.showWeather(lastCity, lastDescription, lastTemperature, lastHumidity);
            view.showIcon(icon);
        } else
        {
            loadWeatherIfAllPermissionsGranted(view);
        }
    }

    @Override
    public void onViewDetached(WeatherFragmentView view)
    {
        locationProvider.removeOnLocationChangedListener(this);
        getView().showIcon(null);
    }

    @Override
    public void onDestroyed()
    {
        locationProvider.destroy();
        super.onDestroyed();
    }

    private void loadWeatherIfAllPermissionsGranted(WeatherFragmentView view)
    {
        if (!requestPermissionsIfNeeded(view))
        {
            Location lastKnownLocation = locationProvider.lastLocation();
            loadWeather(lastKnownLocation);
        }
    }

    @Event
    public void onEventPermissionsResult(PermissionEvent permission)
    {
        onPermissionsResult(permission.getRequestCode(), permission.getPermissions(), permission.getGrantResults());
    }

    private void loadWeather(Location location)
    {
        if (location != null)
        {
            final double longitude = location.getLongitude();
            final double latitude = location.getLatitude();
            submit("loadWeather", () -> loadWeather(longitude, latitude));
        }
    }

    protected void dispatchRequestStarted()
    {
        submitOnUiThread(() -> getView().requestStarted());
    }

    protected void dispatchRequestFinished()
    {
        submitOnUiThread(() -> getView().requestFinished());
    }

    private boolean requestPermissionsIfNeeded(WeatherFragmentView view)
    {
        boolean result = false;
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
        {
            view.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_PERM_ACCESS_FINE_LOCATION);
            result = true;
        }
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
        {
            view.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION);
            result = true;
        }
        return result;
    }

    protected void updateState(Weather weather)
    {
        this.lastDescription = weather.description();
        this.lastCity = weather.city();
        this.lastTemperature = weather.temperature();
        this.lastHumidity = weather.humidity();
    }

    private boolean hasAllPermissions()
    {
        WeatherFragmentView view = getView();
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
            return false;
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
            return false;
        return true;
    }

    public void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == REQUEST_CODE_PERM_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (getView().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
                loadWeather(locationProvider.lastLocation());
        } else if (requestCode == REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (getView().isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
                loadWeather(locationProvider.lastLocation());
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if (hasAllPermissions() && isNewLocation(location))
        {
            this.lastLocation = location;
            loadWeather(location);
        }
    }

    private boolean isNewLocation(Location location)
    {
        if (lastLocation == null) {
            return true;
        }

        if (lastLocation.getLongitude() != location.getLongitude()) {
            return true;
        }

        if (lastLocation.getLatitude() != location.getLatitude()) {
            return true;
        }

        return false;
    }

    @Override
    public void onIconLoaded(final Bitmap resource)
    {
        this.icon = resource;
        submitOnUiThread(() -> getView().showIcon(resource));
    }
}
