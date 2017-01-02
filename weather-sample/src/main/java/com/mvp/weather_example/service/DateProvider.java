package com.mvp.weather_example.service;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Andy on 23.12.2016.
 */

public class DateProvider {

    public Calendar getCurrentDate(){
        Calendar instance = Calendar.getInstance();
        instance.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        instance.setTimeInMillis(System.currentTimeMillis());
        return instance;
    }

}
