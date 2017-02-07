package com.mvp.weather_example.service;

import com.mvp.weather_example.model.forecast.threehours.List;
import com.mvp.weather_example.model.forecast.threehours.ThreeHoursForecastWeather;

import java.text.ParseException;
import java.util.Calendar;

public abstract class WeatherResponseFilter
{

    protected DateProvider dateProvider;

    public WeatherResponseFilter(DateProvider dateProvider)
    {
        this.dateProvider = dateProvider;
    }

    protected abstract Calendar getCurrentDate();

    public String parse(ThreeHoursForecastWeather body) throws ParseException
    {
        StringBuilder sb = new StringBuilder();
        boolean foundAnEntryPreviously = false;
        Calendar currentDate = getCurrentDate();
        for (List entry : body.getList())
        {
            boolean result = parse(sb, currentDate, entry);
            if (foundAnEntryPreviouslyButNotThisTime(foundAnEntryPreviously, result))
                break;
            else if (result)
                foundAnEntryPreviously = true;
        }
        return sb.toString();
    }

    private boolean foundAnEntryPreviouslyButNotThisTime(boolean foundAnEntryPreviously, boolean thisTime)
    {
        return foundAnEntryPreviously && !thisTime;
    }

    private boolean parsedDateDoesNotRepresentThePast(Calendar currentDate, Calendar parsedDate)
    {
        return currentDate.compareTo(parsedDate) < 0;
    }

    protected abstract boolean isCorrectDay(Calendar currentDate, Calendar parsedDate);

    private boolean parse(StringBuilder sb, Calendar currentDate, List list) throws ParseException
    {
        String strDate = list.getDtTxt();
        Calendar parsedDate = dateProvider.parse(strDate);
        if (isCorrectDay(currentDate, parsedDate) && parsedDateDoesNotRepresentThePast(currentDate, parsedDate))
        {
            sb.append(strDate)
              .append(": ")
              .append(list.getMain().getTemp())
              .append("°C")
              .append("\n");
            return true;
        }else{
            return false;
        }
    }

}
