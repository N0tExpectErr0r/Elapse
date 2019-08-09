package com.n0texpecterr0r.elapse_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.n0texpecterr0r.elapse.MethodEventManager;
import com.n0texpecterr0r.elapse.MethodObserver;
import com.n0texpecterr0r.elapse.TrackMethod;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService mExecutor = Executors.newFixedThreadPool(10);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int i = 0; i < 10; i++) {
            mExecutor.execute(this::test);
        }
    }

    @TrackMethod(tag = "TIME")
    public void test() {
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
