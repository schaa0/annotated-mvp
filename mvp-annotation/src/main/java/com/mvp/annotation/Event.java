package com.mvp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Event {
    static String UI_THREAD = "ui";
    static String BACKGROUND_THREAD = "background";
    String thread() default UI_THREAD;
}
