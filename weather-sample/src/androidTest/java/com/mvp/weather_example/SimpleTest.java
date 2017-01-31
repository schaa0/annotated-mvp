package com.mvp.weather_example;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;

import com.mvp.annotation.ApplicationClass;
import com.mvp.annotation.Stub;
import com.mvp.weather_example.di.ModuleProvider;
import com.mvp.weather_example.di.ModuleProviderDelegate;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.model.forecast.tomorrow.TomorrowWeather;
import com.mvp.weather_example.model.today.TodayWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.WeatherService;
import com.mvp.weather_example.stubs.Responses;
import com.mvp.weather_example.stubs.StubDateProvider;
import com.mvp.weather_example.view.MainActivity;
import com.mvp.weather_example.view.TodayWeatherFragment;
import com.mvp.weather_example.view.TomorrowWeatherFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Calendar;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@ApplicationClass(ModuleProvider.class)
public class SimpleTest extends TestCase<ModuleProviderDelegate>
{

    @Mock
    WeatherService weatherService;

    DateProvider dateProvider = new StubDateProvider(2017, Calendar.JANUARY, 22, 23, 0, 0);

    private MainActivity mActivity;

    @Rule
    public CustomActivityTestRule<MainActivity> rule =
            new CustomActivityTestRule<MainActivity>(MainActivity.class)
            {

                @Override
                protected void onInjectDependencies(MainActivity activity)
                {

                }
            };

    @Test
    public void itShouldDisplayTemperatureFromApi()
    {
        when(weatherService.getTomorrowWeather(anyDouble(), anyDouble(), anyString(), anyInt(), anyString()))
                .thenReturn(new WeatherCall<>(TomorrowWeather.class, Responses.TOMORROW_WEATHER));
        when(weatherService.getCurrentWeather(anyDouble(), anyDouble(), anyString(), anyString()))
                .thenReturn(new WeatherCall<>(TodayWeather.class, Responses.TODAY_WEATHER));

        app().with(weatherService).with(dateProvider).apply();

        mActivity = rule.launchActivity(null);

        String tagTomorrow = "android:switcher:" + R.id.container + ":1";
        TomorrowWeatherFragment tomorrowWeatherFragment =
                (TomorrowWeatherFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tagTomorrow);
        assertEquals("7.85°C", tomorrowWeatherFragment.temperatureTextView.getText().toString());

        String tagToday = "android:switcher:" + R.id.container + ":0";
        TodayWeatherFragment todayWeatherFragment =
                (TodayWeatherFragment) mActivity.getSupportFragmentManager().findFragmentByTag(tagToday);
        assertEquals("6.0", todayWeatherFragment.temperatureTextView.getText().toString());
    }

    @Test
    public void itShouldShowWeatherForNextDay()
    {

        when(weatherService.getForecastWeather(anyDouble(), anyDouble(), anyString(), anyString()))
                .thenReturn(new WeatherCall<>(ThreeHoursForecastWeather.class, Responses.THREE_HOUR_FORECAST));
        app().with(weatherService).with(dateProvider).apply();
        mActivity = rule.launchActivity(null);

        ViewInteraction appCompatTextView = onView(
                allOf(withText("Tomorrow"), isDisplayed()));
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

}
