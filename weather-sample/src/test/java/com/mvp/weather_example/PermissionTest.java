package com.mvp.weather_example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.mvp.PresenterType;
import com.mvp.TestCase;
import com.mvp.TodayWeatherFragmentController;
import com.mvp.TodayWeatherPresenterBuilder;
import com.mvp.ViewType;
import com.mvp.weather_example.di.TestModuleProvider;
import com.mvp.weather_example.di.ViewPagerFragmentFactory;
import com.mvp.weather_example.event.PermissionEvent;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.view.MainActivity;
import com.mvp.weather_example.view.TodayWeatherFragment;
import com.mvp.weather_example.view.WeatherView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.support.v4.SupportFragmentController;
import org.robolectric.util.ActivityController;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, application = TestModuleProvider.class)
public class PermissionTest extends TestCase
{

    private TestModuleProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = (TestModuleProvider) RuntimeEnvironment.application;
        provider.with(new ViewPagerFragmentFactory(provider.getApplicationContext()){
            @Override
            public Fragment getItem(int position)
            {
                return new Fragment();
            }
        });
    }

    @Test
    public void testPresenterReceivesPermissionWhenRequestedFromActivity() {

        TodayWeatherPresenterBuilder builder = new TodayWeatherPresenterBuilder(
                new TodayWeatherFragmentController(new TodayWeatherFragment(), MainActivity.class), provider)
                .in(R.id.container);

        TodayWeatherPresenterBuilder.BindingResult bindingResult = configurePresenter(builder, ViewType.REAL, PresenterType.MOCK);
        TodayWeatherPresenter presenter = bindingResult.presenter();

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
        controller.setup();

        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
        int[] grantResults = {PackageManager.PERMISSION_GRANTED};
        MainActivity mainActivity = controller.get();
        mainActivity.onRequestPermissionsResult(1, permissions, grantResults);

        verify(presenter).onEventPermissionsResult(eq(new PermissionEvent(1, permissions, grantResults)));
    }

    @Test
    public void mainActivityReceivesErrorIfLoadingWeatherFails() throws IOException
    {
        WeatherService weatherService = mock(WeatherService.class);
        when(weatherService.getCurrentWeather(anyDouble(), anyDouble(), anyString()))
            .thenThrow(new IOException("Loading weather failed: with some internal exception message..."));

        LocationProvider locationProvider = mock(LocationProvider.class);
        when(locationProvider.lastLocation()).thenReturn(createLocation(1.0, 1.0));

        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class);
        controller.setup();

        TodayWeatherPresenterBuilder builder = new TodayWeatherPresenterBuilder(
                new TodayWeatherFragmentController(new TodayWeatherFragment(), MainActivity.class), provider)
                .parameter(weatherService)
                .parameter(locationProvider)
                .in(R.id.container);

        WeatherView mockView = mock(WeatherView.class);
        doReturn(true).when(mockView).isPermissionGranted(anyString());
        TodayWeatherPresenterBuilder.BindingResult bindingResult = configurePresenter(builder, mockView, PresenterType.REAL);
        when(bindingResult.view().isPermissionGranted(anyString())).thenReturn(true);

        assertTrue(ShadowToast.getTextOfLatestToast().startsWith("Loading weather failed:"));

    }

    @Test
    public void testPresenterReceivesPermissionWhenNotNeeded() {

        TodayWeatherPresenterBuilder builder = new TodayWeatherPresenterBuilder(
                new TodayWeatherFragmentController(new TodayWeatherFragment(), MainActivity.class), provider)
                .in(R.id.container);

        TodayWeatherPresenterBuilder.BindingResult bindingResult = configurePresenter(builder, ViewType.REAL, PresenterType.MOCK);
        TodayWeatherPresenter presenter = bindingResult.presenter();

        SupportFragmentController<TodayWeatherFragment> controller = bindingResult.controller();
        TodayWeatherFragment fragment = controller.get();
        fragment.requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION, TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION);
        String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
        int[] grantResults = {PackageManager.PERMISSION_GRANTED};
        verify(presenter).onPermissionsResult(TodayWeatherPresenter.REQUEST_CODE_PERM_ACCESS_COARSE_LOCATION, permissions, grantResults);
    }

    @NonNull
    private Location createLocation(double longitude, double latitude) {
        Location location = new Location("");
        location.setLatitude(longitude);
        location.setLongitude(latitude);
        return location;
    }

}
