package com.mvp.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({ TYPE, FIELD })
public @interface Presenter {
    ViewEvent[] viewEvents() default {};
    Class<?> viewImplementation() default Object.class;
    Class<?>[] needsModules() default {};
    Class<?>[] needsComponents() default {};
}
