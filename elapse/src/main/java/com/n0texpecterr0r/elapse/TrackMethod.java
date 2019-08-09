package com.n0texpecterr0r.elapse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface TrackMethod {
    String tag();
}
