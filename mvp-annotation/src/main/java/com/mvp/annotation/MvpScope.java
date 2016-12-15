package com.mvp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by Andy on 13.12.2016.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface MvpScope {
}
