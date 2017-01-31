package com.mvp.weather_example;

import com.google.gson.Gson;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.TomorrowWeatherResponseFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Andy on 31.01.2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class UnitTestWeatherParser
{

    @Mock
    private DateProvider dateProvider;

    @InjectMocks
    private TodayWeatherResponseFilter todayWeatherResponseFilter;

    @InjectMocks
    private TomorrowWeatherResponseFilter tomorrowWeatherResponseFilter;

    private Calendar createCurrentDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar instance = Calendar.getInstance(Locale.GERMANY);
        instance.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        instance.set(year, month, day, hour, minute, second);
        return instance;
    }

    @Test
    public void itShouldFilterOutAllRecordsOlderThanProvidedDate() {
        Calendar calendar = createCurrentDate(2016, Calendar.DECEMBER, 23, 12, 0, 1);
        when(dateProvider.getCurrentDate()).thenReturn(calendar);
        ThreeHoursForecastWeather body = new Gson().fromJson(Responses.FORECAST_RESULT, ThreeHoursForecastWeather.class);
        String actual = todayWeatherResponseFilter.parse(body);
        assertEquals(Responses.createExpectedResult(), actual);
    }

    @Test
    public void itShouldRecognizeYearChange() {
        Calendar calendar = createCurrentDate(2016, Calendar.DECEMBER, 31, 12, 0, 0);
        when(dateProvider.getCurrentDate()).thenReturn(calendar);
        ThreeHoursForecastWeather body = new Gson().fromJson(Responses.FORECAST_WITH_YEAR_CHANGE_RESULT, ThreeHoursForecastWeather.class);
        String actual = tomorrowWeatherResponseFilter.parse(body);
        assertEquals(new StringBuilder()
                .append("2017-01-01 00:00:00: -11.13°C").append("\n")
                .append("2017-01-01 03:00:00: -11.27°C").append("\n")
                .toString(), actual);
    }

}
