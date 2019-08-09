package com.n0texpecterr0r.elapse;

/**
 * Created by N0tExpectErr0r at 2019/08/08
 */
public interface MethodObserver {
    default void onMethodEnter(String tag, String methodName) {}
    default void onMethodExit(String tag, String methodName) {}
}
