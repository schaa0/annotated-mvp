package com.mvp.weather_example.di;

import android.support.v4.app.Fragment;

import com.mvp.weather_example.view.TodayWeatherFragment;
import com.mvp.weather_example.view.TomorrowWeatherFragment;

import javax.inject.Inject;

public class ViewPagerFragmentFactory
{

    private static final int PAGE_COUNT = 2;

    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new TodayWeatherFragment();
            case 1:
                return new TomorrowWeatherFragment();
            default:
                throw new IllegalArgumentException(String.format("invalid position : %d", position));
        }
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position) {
            case 0:
                return "Today";
            case 1:
                return "Tomorrow";
            default:
                throw new IllegalArgumentException(String.format("invalid position : %d", position));
        }
    }

    public int getCount()
    {
        return PAGE_COUNT;
    }
}
