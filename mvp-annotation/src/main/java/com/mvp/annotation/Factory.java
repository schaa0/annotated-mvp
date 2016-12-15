package com.mvp.annotation;

/**
 * Created by Andy on 01.12.2016.
 */

public @interface Factory {
    Class<?>[] presenters() default {};
}
