package com.mvp.weather_example;

import com.google.gson.Gson;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;
import com.mvp.weather_example.service.DateProvider;
import com.mvp.weather_example.service.TodayWeatherResponseFilter;
import com.mvp.weather_example.service.TomorrowWeatherResponseFilter;
import com.mvp.weather_example.stubs.StubDateProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class UnitTestWeatherParser
{

    private TodayWeatherResponseFilter todayWeatherResponseFilter;
    private TomorrowWeatherResponseFilter tomorrowWeatherResponseFilter;

    @Before
    public void setUp() throws Exception {

    }

    private void createFilters(DateProvider dateProvider) {
        todayWeatherResponseFilter = new TodayWeatherResponseFilter(dateProvider);
        tomorrowWeatherResponseFilter = new TomorrowWeatherResponseFilter(dateProvider);
    }

    @Test
    public void itShouldFilterOutAllRecordsOlderThanProvidedDate() throws ParseException
    {
        DateProvider dateProvider = new StubDateProvider(2016, Calendar.DECEMBER, 23, 12, 0, 1);
        createFilters(dateProvider);
        ThreeHoursForecastWeather body = new Gson().fromJson(Responses.FORECAST_RESULT, ThreeHoursForecastWeather.class);
        String actual = todayWeatherResponseFilter.parse(body);
        assertEquals(Responses.createExpectedResult(), actual);
    }

    @Test
    public void itShouldRecognizeYearChange() throws ParseException
    {
        DateProvider dateProvider = new StubDateProvider(2016, Calendar.DECEMBER, 31, 12, 0, 0);
        createFilters(dateProvider);
        ThreeHoursForecastWeather body = new Gson().fromJson(Responses.FORECAST_WITH_YEAR_CHANGE_RESULT, ThreeHoursForecastWeather.class);
        String actual = tomorrowWeatherResponseFilter.parse(body);
        assertEquals(new StringBuilder()
                .append("2017-01-01 00:00:00: -11.13°C").append("\n")
                .append("2017-01-01 03:00:00: -11.27°C").append("\n")
                .toString(), actual);
    }

}
