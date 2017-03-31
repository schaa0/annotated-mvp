package com.mvp.weather_example;

import com.mvp.FragmentRouter;

import org.mockito.invocation.InvocationOnMock;

import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;

public class StubbedFragmentRouter extends FragmentRouter {

    private Class<?> target;

    public StubbedFragmentRouter() {
        super(null);
    }

    @Override
    public FragmentNavigation navigateTo(Class<?> target)
    {
        this.target = target;
        FragmentNavigation mock = mock(FragmentNavigation.class, invocation -> handleInvocation(invocation));
        return mock;
    }

    private FragmentNavigation handleInvocation(InvocationOnMock _invocation) throws Throwable {
        if (_invocation.getMethod().getReturnType().equals(void.class)) {
            return (FragmentNavigation) RETURNS_DEFAULTS.answer(_invocation);
        }else {
            return (FragmentNavigation) _invocation.getMock();
        }
    }

    public Class<?> getLastTarget()
    {
        return target;
    }

}
