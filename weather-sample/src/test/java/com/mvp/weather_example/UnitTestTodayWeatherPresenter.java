package com.mvp.weather_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.mvp.MvpEventBus;
import com.mvp.annotation.ApplicationClass;
import com.mvp.weather_example.di.ModuleProvider;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.today.TodayWeather;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.WeatherResponseFilter;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.IWeatherView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.concurrent.RoboExecutorService;

import static com.mvp.weather_example.Responses.createExpectedResult;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("MissingPermission")
@Config(sdk = 21, constants = com.mvp.weather_example.BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
@ApplicationClass(ModuleProvider.class)
public class UnitTestTodayWeatherPresenter extends PresenterUnitTestCase
{

    public static final int RC_PERM_FINE_LOCATION = TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_FINE_LOCATION;
    public static final int RC_PERM_COARSE_LOCATION = TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION;
    public static final String PERM_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERM_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    @Mock
    private LocationManager locationManager;
    @Mock
    private WeatherResponseFilter weatherParser;
    @Mock
    private ImageRequestManager requestManager;
    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private TodayWeatherPresenter presenter;

    private IWeatherView view;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        view = mock(IWeatherView.class);
        presenter.setView(view);
        injectFields(presenter);
    }

    @After
    public void tearDown() throws Exception {

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
        verifyZeroInteractions(locationManager);
        presenter.onPermissionsResult(RC_PERM_COARSE_LOCATION, new String[]{PERM_COARSE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED});
        verifyZeroInteractions(locationManager);
        presenter.onPermissionsResult(TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_FINE_LOCATION, new String[]{PERM_FINE_LOCATION}, new int[]{PackageManager.PERMISSION_DENIED});
        verifyZeroInteractions(locationManager);
    }

    @Test
    public void mustNotRequestWeatherIfThereIsNoLastKnownLocation() {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn("gps");
        when(locationManager.getLastKnownLocation(anyString())).thenReturn(null);
        presenter.onViewAttached(view);
        verifyZeroInteractions(weatherService);
    }

    @Test
    public void shouldRequestWeatherIfPermissionIsGrantedAndLocationIsPresent() {
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn("gps");
        Location location = createLocation(1.0, 1.0);
        when(weatherService.getCurrentWeather(1.0, 1.0, "metric", WeatherService.API_KEY))
                .thenReturn(new WeatherCall<>(TodayWeather.class, Responses.TODAY_WEATHER));
        when(locationManager.getLastKnownLocation(anyString())).thenReturn(location);

        presenter.onViewAttached(view);
        verify(view, atLeastOnce()).isPermissionGranted(anyString());
        verify(view).requestStarted();
        verify(view).showWeather("3.76", "100");
        verify(view).requestFinished();
        verifyZeroInteractions(view);
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
        reset(locationManager, view);
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn("gps");
        when(locationManager.getLastKnownLocation(anyString())).thenReturn(null);
        presenter.loadForecastWeatherDataForToday();
        verifyZeroInteractions(weatherService, view);
    }

    @Test
    public void shouldLoadForecastsIfLocationIsPresent() {
        presenter.onViewAttached(view);
        reset(locationManager, view);
        when(view.isPermissionGranted(PERM_COARSE_LOCATION)).thenReturn(true);
        when(view.isPermissionGranted(PERM_FINE_LOCATION)).thenReturn(true);
        when(locationManager.getBestProvider(any(Criteria.class), anyBoolean())).thenReturn("gps");
        double longitude = 1.0;
        double latitude = 1.0;
        when(locationManager.getLastKnownLocation(anyString())).thenReturn(createLocation(longitude, latitude));
        when(weatherParser.parse(any(ThreeHoursForecastWeather.class))).thenReturn(createExpectedResult());
        when(weatherService.getForecastWeather(longitude, latitude, "metric", WeatherService.API_KEY))
                .thenReturn(new WeatherCall<>(ThreeHoursForecastWeather.class, Responses.FORECAST_RESULT));
        presenter.loadForecastWeatherDataForToday();
        String expected = createExpectedResult();
        verify(view).requestStarted();
        verify(view).showForecastWeather(expected);
        verify(view).requestFinished();
    }

}