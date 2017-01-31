package com.mvp.weather_example.service;

import java.util.Calendar;

import javax.inject.Inject;

/**
 * Created by Andy on 31.01.2017.
 */

public class TodayWeatherResponseFilter extends WeatherResponseFilter
{
    @Inject
    public TodayWeatherResponseFilter(DateProvider dateProvider)
    {
        super(dateProvider);
    }

    @Override
    protected Calendar getCurrentDate()
    {
        return dateProvider.getCurrentDate();
    }

    @Override
    protected boolean isCorrectDay(Calendar currentDate, Calendar parsedDate)
    {
        if (currentDate.get(Calendar.YEAR) != parsedDate.get(Calendar.YEAR)) return false;
        if (currentDate.get(Calendar.MONTH) != parsedDate.get(Calendar.MONTH)) return false;
        return (currentDate.get(Calendar.DAY_OF_YEAR) == parsedDate.get(Calendar.DAY_OF_YEAR));
    }

}
