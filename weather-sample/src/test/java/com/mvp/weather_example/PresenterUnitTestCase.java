package com.mvp.weather_example;

import android.os.Handler;
import android.os.Looper;

import com.mvp.MvpEventBus;
import com.mvp.weather_example.presenter.TodayWeatherPresenter;

import org.robolectric.util.concurrent.RoboExecutorService;

import java.lang.reflect.Field;

public class PresenterUnitTestCase
{

    protected void injectFields(Object presenter)
    {
        try
        {
            findAndSetField(presenter, "executorService", new RoboExecutorService());
            findAndSetField(presenter, "handler", new Handler(Looper.myLooper()));
            findAndSetField(presenter, "eventBus", new MvpEventBus());
        } catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        } catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void findAndSetField(Object obj, String fieldName, Object objectToInject) throws NoSuchFieldException, IllegalAccessException {
        findAndSetField(obj, obj.getClass(), fieldName, objectToInject);
    }

    private void findAndSetField(Object obj, Class<?> clazz, String fieldName, Object objectToInject) throws IllegalAccessException
    {
        if (clazz == null || clazz.equals(Object.class))
            return;
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (Exception e) {

        }
        if (field == null)
            findAndSetField(obj, clazz.getSuperclass(), fieldName, objectToInject);
        else {
            field.setAccessible(true);
            field.set(obj, objectToInject);
        }
    }

}
