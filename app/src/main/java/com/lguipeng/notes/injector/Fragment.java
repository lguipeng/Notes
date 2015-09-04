package com.lguipeng.notes.injector;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by lgp on 2015/9/3.
 */
@Scope
@Retention(RUNTIME)
public @interface Fragment {
}
