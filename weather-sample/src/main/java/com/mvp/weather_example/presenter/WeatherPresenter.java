package com.mvp.weather_example.presenter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.mvp.MvpPresenter;
import com.mvp.annotation.BackgroundThread;
import com.mvp.annotation.Event;
import com.mvp.weather_example.event.PermissionEvent;
import com.mvp.weather_example.model.Weather;
import com.mvp.weather_example.model.forecast.threehours.List;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.IWeatherView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Andy on 22.12.2016.
 */

public abstract class WeatherPresenter extends MvpPresenter<IWeatherView> implements LocationListener{

    public static final int REQUEST_CODE_PERM_ACCESS_FINE_LOCATION = 45;
    public static final int REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION = 23;
    public static final int MIN_LOCATION_UPDATE_INTERVAL = 60000;
    public static final int MIN_DISTANCE_IN_METERS = 100;

    protected LocationManager locationManager;
    protected WeatherService weatherService;
    protected ImageRequestManager requestManager;
    protected DateProvider dateProvider;

    protected String lastTemperature;
    protected String lastHumidity;
    private Bitmap icon;
    private String currentProvider;

    protected WeatherPresenter() { }

    public WeatherPresenter(LocationManager locationManager, WeatherService weatherService, ImageRequestManager requestManager, DateProvider dateProvider){
        this.locationManager = locationManager;
        this.weatherService = weatherService;
        this.requestManager = requestManager;
        this.dateProvider = dateProvider;
    }

    private Location lastLocation(){
        return getLastKnownLocation(getBestProvider());
    }

    @Override
    public void onViewAttached(IWeatherView view) {
        loadWeatherIfAllPermissionsGranted(view);
    }

    private void loadWeatherIfAllPermissionsGranted(IWeatherView view) {
        if (!requestPermissionsIfNeeded(view)) {
            String bestProvider = getBestProvider();
            //noinspection MissingPermission
            requestLocationUpdates(bestProvider);
            Location lastKnownLocation = getLastKnownLocation(bestProvider);
            loadWeather(lastKnownLocation);
        }
    }

    private void requestLocationUpdates(String bestProvider) {
        locationManager.requestLocationUpdates(bestProvider, MIN_LOCATION_UPDATE_INTERVAL, MIN_DISTANCE_IN_METERS, this);
    }

    @Event
    public void onEventPermissionsResult(PermissionEvent permission) {
        onPermissionsResult(permission.getRequestCode(), permission.getPermissions(), permission.getGrantResults());
    }

    private void loadWeather(Location location) {
        if (location != null) {
            final double longitude = location.getLongitude();
            final double latitude = location.getLatitude();
            submit("loadWeather", new Runnable() {
                @Override
                public void run() {
                    loadWeather(longitude, latitude);
                }
            });
        }
    }

    private void dispatchRequestStarted() {
        submitOnUiThread(new Runnable() {
            @Override
            public void run() {
                getView().requestStarted();
            }
        });
    }

    protected void internalShowForecastWeather(Call<ThreeHoursForecastWeather> call, Calendar currentDate) {
        try {
            dispatchRequestStarted();
            Response<ThreeHoursForecastWeather> execute = call.execute();
            ThreeHoursForecastWeather body = execute.body();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
            StringBuilder sb = new StringBuilder();
            boolean foundAnEntry = false;
            for (List list : body.getList()) {
                String strDate = list.getDtTxt();
                try {
                    Calendar parsedDate = Calendar.getInstance(Locale.GERMANY);
                    parsedDate.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                    parsedDate.setTime(dateFormat.parse(strDate));
                    if (isCorrectDay(currentDate, parsedDate) && parsedDateDoesntRepresentThePast(currentDate, parsedDate)){
                        foundAnEntry = true;
                        sb.append(dateFormat.format(parsedDate.getTime())).append(": ").append(list.getMain().getTemp()).append("°C").append("\n");
                    }else if(foundAnEntry)
                        break;

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            final String forecastWeather = sb.toString();
            submitOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getView().requestFinished();
                    getView().showForecastWeather(forecastWeather);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean parsedDateDoesntRepresentThePast(Calendar currentDate, Calendar parsedDate) {
        return currentDate.compareTo(parsedDate) < 0;
    }

    protected abstract boolean isCorrectDay(Calendar currentDate, Calendar parsedDate);

    @BackgroundThread
    public abstract void loadWeather(double longitude, double latitude);

    private String getBestProvider() {
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        currentProvider = locationManager.getBestProvider(criteria, false);
        return currentProvider;
    }

    private Location getLastKnownLocation(String bestProvider) {
        try{
            return locationManager.getLastKnownLocation(bestProvider);
        }catch(SecurityException e){

        }
        return null;
    }

    protected Location getLastKnownLocation(){
        return getLastKnownLocation(getBestProvider());
    }

    private boolean requestPermissionsIfNeeded(IWeatherView view) {
        boolean result = false;
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            view.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_CODE_PERM_ACCESS_FINE_LOCATION);
            result = true;
        }
        if (!view.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            view.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION);
            result = true;
        }
        return result;
    }

    @Override
    public void onViewReattached(IWeatherView view) {
        if (lastTemperature != null && lastHumidity != null) {
            String bestProvider = getBestProvider();
            //noinspection MissingPermission
            requestLocationUpdates(bestProvider);
            view.showWeather(lastTemperature, lastHumidity);
            view.showIcon(icon);
        }
        else {
            loadWeatherIfAllPermissionsGranted(view);
        }
    }

    @Override
    public void onViewDetached(IWeatherView view) {
        getView().showIcon(null);
        if (hasAllPermissions()){
            //noinspection MissingPermission
            locationManager.removeUpdates(this);
        }
    }

    private boolean hasAllPermissions() {
        IWeatherView view = getView();
        return view.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION) && view.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    protected void internalShowWeather(Call<? extends Weather> call){
        try {
            dispatchRequestStarted();
            final Response<? extends Weather> execute = call.execute();
            if (execute.isSuccessful()){
                final Weather weather = execute.body();
                lastTemperature = weather.temperature();
                lastHumidity = weather.humidity();
                submitOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getView().requestFinished();
                        loadIcon(weather.icon());
                        getView().showWeather(lastTemperature, lastHumidity);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadIcon(String icon) {
        requestManager.load(icon, new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                WeatherPresenter.this.icon = resource;
                if (getView() != null)
                    getView().showIcon(resource);
            }
        });
    }

    public void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if (requestCode == REQUEST_CODE_PERM_ACCESS_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (getView().isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION))
                loadWeather(lastLocation());
        }else if (requestCode == REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if (getView().isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION))
                loadWeather(lastLocation());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        if (currentProvider.equals(s)){
            requestLocationUpdates(s);
            loadWeather(getLastKnownLocation(s));
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (currentProvider.equals(s))
            locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (hasAllPermissions())
            loadWeather(location);
    }

}
