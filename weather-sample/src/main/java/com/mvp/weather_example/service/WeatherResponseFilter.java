package com.mvp.weather_example.service;

import com.mvp.weather_example.model.forecast.threehours.List;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Andy on 31.01.2017.
 */

public abstract class WeatherResponseFilter
{

    protected DateProvider dateProvider;

    public WeatherResponseFilter(DateProvider dateProvider){
        this.dateProvider = dateProvider;
    }

    protected abstract Calendar getCurrentDate();

    public String parse(ThreeHoursForecastWeather body) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
        StringBuilder sb = new StringBuilder();
        boolean foundAnEntry = false;
        Calendar currentDate = getCurrentDate();
        for (List list : body.getList()) {
            String strDate = list.getDtTxt();
            try {
                Calendar parsedDate = Calendar.getInstance(Locale.GERMANY);
                parsedDate.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
                parsedDate.setTime(dateFormat.parse(strDate));
                if (isCorrectDay(currentDate, parsedDate) && parsedDateDoesntRepresentThePast(currentDate, parsedDate)){
                    foundAnEntry = true;
                    sb.append(dateFormat.format(parsedDate.getTime())).append(": ").append(list.getMain().getTemp()).append("°C").append("\n");
                }else if(foundAnEntry)
                    break;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        final String forecastWeather = sb.toString();
        return forecastWeather;
    }

    private boolean parsedDateDoesntRepresentThePast(Calendar currentDate, Calendar parsedDate) {
        return currentDate.compareTo(parsedDate) < 0;
    }

    protected abstract boolean isCorrectDay(Calendar currentDate, Calendar parsedDate);

}
