package com.mvp.weather_example;

import android.content.Context;

import com.mvp.ActivityRouter;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StubbedActivityRouter extends ActivityRouter
{
    private Class<?> target;

    public StubbedActivityRouter()
    {
        super(null);
    }

    @Override
    public ActivityNavigation navigateTo(Class<?> target)
    {
        this.target = target;
        ActivityNavigation mock = mock(ActivityNavigation.class, invocation -> handleInvocation(invocation));
        return mock;
    }

    private ActivityNavigation handleInvocation(InvocationOnMock _invocation) throws Throwable {
        if (_invocation.getMethod().getReturnType().equals(void.class)) {
            return (ActivityNavigation) RETURNS_DEFAULTS.answer(_invocation);
        }else {
            return (ActivityNavigation) _invocation.getMock();
        }
    }

    public Class<?> getLastTarget()
    {
        return target;
    }
}
