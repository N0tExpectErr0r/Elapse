package com.n0texpecterr0r.elapse_demo;

import android.app.Application;

import com.n0texpecterr0r.elapse.MethodEventManager;

/**
 * Created by N0tExpectErr0r at 2019/08/09
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MethodEventManager.getInstance().registerMethodObserver("TIME", new TimeObserver());
    }
}
