package com.mvp;

/**
 * Created by Andy on 02.12.2016.
 */

public interface ComponentActivity<A> {
    void inject(A injectee);
}
