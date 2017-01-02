package com.mvp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Event {
    String UI_THREAD = "ui";
    String BACKGROUND_THREAD = "background";
    String thread() default UI_THREAD;
    String condition() default "";
}
