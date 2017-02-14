package com.mvp.weather_example.di;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.mvp.weather_example.R;
import com.mvp.weather_example.view.TodayWeatherFragment;
import com.mvp.weather_example.view.TomorrowWeatherFragment;

public class ViewPagerFragmentFactory
{

    private Context context;

    public ViewPagerFragmentFactory(Context context){
        this.context = context;
    }

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
                return context.getString(R.string.today);
            case 1:
                return context.getString(R.string.tomorrow);
            default:
                throw new IllegalArgumentException(String.format("invalid position : %d", position));
        }
    }

    public int getCount()
    {
        return PAGE_COUNT;
    }
}
