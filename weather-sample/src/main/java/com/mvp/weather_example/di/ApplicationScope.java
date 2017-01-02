package com.mvp.weather_example.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by Andy on 27.12.2016.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ApplicationScope { }
