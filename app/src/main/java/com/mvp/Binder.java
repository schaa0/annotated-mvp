package com.mvp;

/**
 * Created by Andy on 13.12.2016.
 */

public interface Binder<A, M> {
    A component(M module);
}
