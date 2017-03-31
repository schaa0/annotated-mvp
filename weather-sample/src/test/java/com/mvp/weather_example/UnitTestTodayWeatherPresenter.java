package com.mvp.weather_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.mvp.weather_example.di.TestWeatherApplication;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.today.TodayWeather;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.filter.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.ThreeHourForecastActivity;
import com.mvp.weather_example.view.WeatherView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.text.ParseException;

import static com.mvp.weather_example.Responses.createExpectedResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("MissingPermission")
@Config(sdk = 21, constants = com.mvp.weather_example.BuildConfig.class, application = TestWeatherApplication.class)
@RunWith(RobolectricTestRunner.class)
public class UnitTestTodayWeatherPresenter extends PresenterUnitTestCase
{

    static final int RC_PERM_FINE_LOCATION = TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_FINE_LOCATION;
    static final int RC_PERM_COARSE_LOCATION = TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION;
    static final String PERM_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    static final String PERM_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    @Mock
    private LocationProvider locationProvider;
    @Mock
    private WeatherResponseFilter weatherParser;
    @Mock
    private WeatherService weatherService;
    @Mock
    private WeatherView view;

    @InjectMocks
    private TodayWeatherPresenter presenter;

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        super.setUp();
        presenter.setView(view);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void viewIsSet() {
        assertNotNull(presenter.getView());
    }

    @Test
    public void shouldRequestPermissionsIfRequired() {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(false);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(false);
        presenter.onViewAttached(view);
        verify(view).requestPermission(PERM_FINE_LOCATION, RC_PERM_FINE_LOCATION);
        verify(view).requestPermission(PERM_COARSE_LOCATION, RC_PERM_COARSE_LOCATION);
    }

    @Test
    public void shouldNotInteractWithLocationManagerIfPermissionIsNotGranted() {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(false);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(false);
        presenter.onViewAttached(view);
        verify(locationProvider, only()).addOnLocationChangedListener(presenter);
        presenter.onPermissionsResult(RC_PERM_COARSE_LOCATION, new String[]{PERM_COARSE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED});
        verifyZeroInteractions(locationProvider);
        presenter.onPermissionsResult(TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_FINE_LOCATION, new String[]{PERM_FINE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED});
        verifyZeroInteractions(locationProvider);
    }

    @Test
    public void mustNotRequestWeatherIfThereIsNoLastKnownLocation() {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationProvider.lastLocation()).thenReturn(null);
        presenter.onViewAttached(view);
        verifyZeroInteractions(weatherService);
    }

    @Test
    public void shouldRequestWeatherIfPermissionIsGrantedAndLocationIsPresent() throws IOException
    {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        Location location = createLocation(1.0, 1.0);
        when(weatherService.getCurrentWeather(1.0, 1.0, "metric"))
                .thenReturn(new Gson().fromJson(Responses.TODAY_WEATHER, TodayWeather.class));
        when(locationProvider.lastLocation()).thenReturn(location);

        presenter.onViewAttached(view);

        verify(view, atLeastOnce()).isPermissionGranted(anyString());
        verify(view).requestStarted();
        verify(view).showWeather("3.76", "100");
        verify(view).requestFinished();
    }

    @NonNull
    private Location createLocation(double longitude, double latitude) {
        Location location = new Location("");
        location.setLatitude(longitude);
        location.setLongitude(latitude);
        return location;
    }

    @Test
    public void shouldNotLoadForecastsIfNoLocationIsPresent() {
        presenter.onViewAttached(view);
        reset(locationProvider, view);
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationProvider.lastLocation()).thenReturn(null);
        presenter.loadForecastWeatherDataForToday();
        verifyZeroInteractions(weatherService, view);
    }

    @Test
    public void shouldLoadForecastsIfLocationIsPresent() throws ParseException, IOException
    {
        presenter.onViewAttached(view);
        reset(locationProvider, view);
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        double longitude = 1.0;
        double latitude = 1.0;
        when(locationProvider.lastLocation()).thenReturn(createLocation(longitude, latitude));
        when(weatherParser.parse(any(ThreeHoursForecastWeather.class))).thenReturn(createExpectedResult());
        when(weatherService.getForecastWeather(longitude, latitude, "metric"))
                .thenReturn(new Gson().fromJson(Responses.FORECAST_RESULT, ThreeHoursForecastWeather.class));
        presenter.loadForecastWeatherDataForToday();
        String expected = createExpectedResult();
        verify(view).requestStarted();
        assertEquals(getRouter().getLastTarget(), ThreeHourForecastActivity.class);
        verify(view).requestFinished();
    }

}