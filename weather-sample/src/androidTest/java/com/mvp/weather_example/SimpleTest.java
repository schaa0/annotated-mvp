package com.mvp.weather_example;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;

import com.bumptech.glide.RequestManager;
import com.mvp.CustomActivityTestRule;
import com.mvp.uiautomator.UiAutomatorTestCase;
import com.mvp.weather_example.di.AndroidTestWeatherApplication;
import com.mvp.weather_example.di.TestDaggerComponentSingleton;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
import com.mvp.weather_example.model.today.TodayWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.ImageRequestManager;
import com.mvp.weather_example.service.LocationProvider;
import com.mvp.weather_example.service.WeatherApi;
import com.mvp.weather_example.stubs.Responses;
import com.mvp.weather_example.stubs.StubDateProvider;
import com.mvp.weather_example.view.MainActivity;
import com.mvp.weather_example.view.TodayWeatherFragment;
import com.mvp.weather_example.view.TomorrowWeatherFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Calendar;

import javax.inject.Provider;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SimpleTest extends UiAutomatorTestCase<AndroidTestWeatherApplication>
{

    public static final double FAKE_LONGITUDE = 1.0;
    public static final double FAKE_LATITUDE = 1.0;

    @Mock
    WeatherApi weatherApi;
    @Mock
    ImageRequestManager imageRequestManager;
    @Mock
    LocationProvider locationProvider;

    DateProvider dateProvider = new StubDateProvider(2017, Calendar.JANUARY, 22, 23, 0, 0);

    private MainActivity mActivity;

    @Rule
    public CustomActivityTestRule<MainActivity> rule = new CustomActivityTestRule<MainActivity>(MainActivity.class);

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        doReturn(fakeLocation()).when(locationProvider).lastLocation();
        doNothing().when(imageRequestManager).load(anyString(), ArgumentMatchers.any(ImageRequestManager.IconCallback.class));

        dependencies().withImageRequestManager(requestManager -> imageRequestManager)
                      .withDateProvider(() -> dateProvider)
                      .withWeatherApi(() -> weatherApi)
                      .withLocationProvider(locationManager -> locationProvider);
    }

    private Location fakeLocation()
    {
        Location location = new Location("");
        location.setLongitude(FAKE_LONGITUDE);
        location.setLatitude(FAKE_LATITUDE);
        return location;
    }

    @Test
    public void itShouldShowWeatherForNextDay() throws IOException
    {
        when(weatherApi.getForecastWeather(eq(FAKE_LONGITUDE), eq(FAKE_LATITUDE), eq("metric"), eq("de"), anyString())).thenReturn(
                new WeatherCall<>(ThreeHoursForecastWeather.class, Responses.THREE_HOUR_FORECAST)
        );

        dependencies().apply();

        mActivity = rule.launchActivity(null);
        allowPermissionsIfNeeded();

        ViewInteraction appCompatTextView = onView(
                allOf(withText(R.string.tomorrow), isDisplayed()));
        appCompatTextView.perform(click());

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.imageView), isDisplayed()));
        appCompatImageView.perform(click());

        String threeHourForecastDataAsString =
                "2017-01-23 00:00:00: -11.55°C\n" +
                        "2017-01-23 03:00:00: -12.0°C\n" +
                        "2017-01-23 06:00:00: -12.15°C\n" +
                        "2017-01-23 09:00:00: -11.34°C\n" +
                        "2017-01-23 12:00:00: -8.84°C\n" +
                        "2017-01-23 15:00:00: -7.94°C\n" +
                        "2017-01-23 18:00:00: -9.35°C\n" +
                        "2017-01-23 21:00:00: -10.29°C\n";

        ViewInteraction textView = onView(withId(android.R.id.message));
        textView.check(matches(withText(threeHourForecastDataAsString)));

        ViewInteraction button = onView(withId(android.R.id.button1));
        button.check(matches(isDisplayed()));

        ViewInteraction textView2 = onView(withId(R.id.alertTitle));
        textView2.check(matches(withText("Three Hour Forecast")));

    }

    @Test
    public void itShouldDisplayTemperatureFromApi() throws IOException
    {
        doReturn(new WeatherCall<>(TomorrowWeather.class, Responses.TOMORROW_WEATHER))
                .when(weatherApi).getTomorrowWeather(eq(FAKE_LONGITUDE), eq(FAKE_LATITUDE), eq("metric"), eq(1), eq("de"), anyString());

        doReturn(new WeatherCall<>(TodayWeather.class, Responses.TODAY_WEATHER))
                .when(weatherApi).getCurrentWeather(eq(FAKE_LONGITUDE), eq(FAKE_LATITUDE), eq("metric"), eq("de"), anyString());

        dependencies().apply();

        mActivity = rule.launchActivity(null);
        allowPermissionsIfNeeded();

        String tagToday = "android:switcher:" + R.id.container + ":0";
        TodayWeatherFragment todayWeatherFragment =
                (TodayWeatherFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tagToday);
        assertEquals("Temperature: 6.0°C", todayWeatherFragment.temperatureTextView.getText().toString());

        String tagTomorrow = "android:switcher:" + R.id.container + ":1";
        TomorrowWeatherFragment tomorrowWeatherFragment =
                (TomorrowWeatherFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tagTomorrow);
        assertEquals("Temperature: 7.85°C", tomorrowWeatherFragment.temperatureTextView.getText().toString());
    }

}
