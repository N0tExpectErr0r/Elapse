package com.n0texpecterr0r.elapse_demo;

import android.util.Log;

import com.n0texpecterr0r.elapse.MethodObserver;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by N0tExpectErr0r at 2019/08/07
 */
public class TimeObserver implements MethodObserver {
    private static final String TAG_METHOD_TIME = "MethodCost";
    private Map<String, Long> enterTimeMap = new HashMap<>();

    @Override
    public void onMethodEnter(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        Long time = System.currentTimeMillis();
        enterTimeMap.put(key, time);
    }

    @Override
    public void onMethodExit(String tag, String methodName) {
        String key = generateKey(tag, methodName);
        Long enterTime = enterTimeMap.get(key);
        if (enterTime == null) {
            throw new IllegalStateException("method exit without enter");
        }
        long cost = System.currentTimeMillis() - enterTime;
        Log.d(TAG_METHOD_TIME, "method " + methodName + " cost "
                + (double)cost/1000 + "s" + " in thread " + Thread.currentThread().getName());
        enterTimeMap.remove(key);
    }

    private String generateKey(String tag, String methodName) {
        return tag + methodName + Thread.currentThread().getName();
    }
}
