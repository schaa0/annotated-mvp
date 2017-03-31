package com.mvp.weather_example;

import android.content.Context;

import com.mvp.Router;

import static org.mockito.Mockito.mock;

/**
 * Created by Andy on 31.03.2017.
 */

public class StubbedRouter extends Router
{
    private Class<?> target;

    public StubbedRouter()
    {
        super(mock(Context.class));
    }

    @Override
    public Navigation navigateTo(Class<?> target)
    {
        this.target = target;
        return super.navigateTo(target);
    }

    public Class<?> getLastTarget()
    {
        return target;
    }
}
