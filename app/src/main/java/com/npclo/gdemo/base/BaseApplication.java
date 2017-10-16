package com.npclo.gdemo.base;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Endless on 2017/7/19.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
