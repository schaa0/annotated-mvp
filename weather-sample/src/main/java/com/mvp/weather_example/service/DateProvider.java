package com.mvp.weather_example.service;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

public class DateProvider {

    @Inject
    public DateProvider() { }

    public Calendar getCurrentDate(){
        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        instance.setTimeInMillis(System.currentTimeMillis());
        return instance;
    }

}
