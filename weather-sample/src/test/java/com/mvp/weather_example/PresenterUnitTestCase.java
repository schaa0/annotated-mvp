package com.mvp.weather_example;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.mvp.MvpEventBus;
import com.mvp.MvpPresenter;
import com.mvp.Router;

import org.junit.After;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.concurrent.RoboExecutorService;

import java.lang.reflect.Field;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class PresenterUnitTestCase
{

    private MvpEventBus eventBus;
    private StubbedRouter router;

    @Before
    public void setUp() throws Exception {
        eventBus = new MvpEventBus(new Handler(Looper.myLooper()), new RoboExecutorService());
        router = new StubbedRouter();
        findAndSetFields();
    }

    public StubbedRouter getRouter()
    {
        return router;
    }

    @After
    public void tearDown() throws Exception {

    }

    private void findAndSetFields() throws IllegalAccessException, NoSuchFieldException
    {
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields)
        {
            if (field.getAnnotation(InjectMocks.class) != null){
                field.setAccessible(true);
                Object obj = field.get(this);
                if (MvpPresenter.class.isAssignableFrom(obj.getClass())){
                    findAndSetField(obj, "executorService", new RoboExecutorService());
                    findAndSetField(obj, "handler", new Handler(Looper.myLooper()));
                    findAndSetField(obj, "eventBus", eventBus);
                    findAndSetField(obj, "router", router);
                }
            }
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
