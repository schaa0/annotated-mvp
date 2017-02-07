package com.mvp.weather_example.stubs;

import com.mvp.weather_example.service.DateProvider;

import java.util.Calendar;

/**
 * Created by Andy on 29.01.2017.
 */

public class StubDateProvider extends DateProvider
{

    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minutes;
    private final int seconds;

    public StubDateProvider(int year, int month, int day, int hour, int minutes, int seconds){
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minutes = minutes;
        this.seconds = seconds;
    }

    @Override
    public Calendar getCurrentDate()
    {
        Calendar calendar = super.getCurrentDate();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        return calendar;
    }
}
