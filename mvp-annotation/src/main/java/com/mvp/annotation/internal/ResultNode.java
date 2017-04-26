package com.mvp.annotation.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface ResultNode
{
    String id();
    String parentId();
    Class<?> dataType();
    Class<?> dependentClass() default Object.class;
    String methodName() default "";
    boolean isInjector();
}
