package com.mvp.weather_example.service;

import java.util.Calendar;

import javax.inject.Inject;

/**
 * Created by Andy on 31.01.2017.
 */

public class TomorrowWeatherResponseFilter extends WeatherResponseFilter
{
    @Inject
    public TomorrowWeatherResponseFilter(DateProvider dateProvider)
    {
        super(dateProvider);
    }

    @Override
    protected Calendar getCurrentDate()
    {
        Calendar currentDate = dateProvider.getCurrentDate();
        currentDate.set(Calendar.HOUR_OF_DAY, 23);
        currentDate.set(Calendar.MINUTE, 59);
        currentDate.set(Calendar.SECOND, 59);
        currentDate.set(Calendar.MILLISECOND, 0);
        return currentDate;
    }

    @Override
    protected boolean isCorrectDay(Calendar currentDate, Calendar parsedDate)
    {
        Calendar clone = Calendar.getInstance();
        clone.setTimeZone(currentDate.getTimeZone());
        clone.setTime(currentDate.getTime());
        clone.add(Calendar.DAY_OF_YEAR, 1);

        return (clone.get(Calendar.DAY_OF_YEAR) == parsedDate.get(Calendar.DAY_OF_YEAR));
    }

}
