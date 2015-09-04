package com.lguipeng.notes.injector;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by lgp on 2015/9/4.
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface ContextLifeCycle {
    String value() default "App";
}
