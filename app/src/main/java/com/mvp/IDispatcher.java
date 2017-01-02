package com.mvp;

public interface IDispatcher<P> {
    void to(Class<?>... targets);
    void toAny();
}
