package com.mvp.weather_example;

import android.app.Application;
import android.support.test.InstrumentationRegistry;

import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.mock;

/**
 * Created by Andy on 23.01.2017.
 */

public abstract class TestCase<T extends Application>
{

    private final T app;

    {
        final AbstractRunner abstractRunner = (AbstractRunner) InstrumentationRegistry.getInstrumentation();
        T app = (T) abstractRunner.getApplication();
        MockitoAnnotations.initMocks(this);
        this.app = app;
    }

    public T app()
    {
        return app;
    }

}
