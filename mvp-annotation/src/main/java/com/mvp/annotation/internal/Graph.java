package com.mvp.annotation.internal;

import com.mvp.annotation.internal.TopNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface Graph
{
    TopNode[] nodes();
}
