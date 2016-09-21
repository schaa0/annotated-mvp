package com.mvp.annotation;

public @interface ViewEvent {
    Class<?> eventType() default Object.class;
    String viewMethodName() default "none";
}
