package com.mvp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Created by Andy on 14.12.2016.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD })
public @interface InjectPresenter {
}
