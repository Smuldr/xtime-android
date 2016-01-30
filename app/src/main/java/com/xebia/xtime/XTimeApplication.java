package com.xebia.xtime;

import android.app.Application;

import timber.log.Timber;

public class XTimeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
    }

    private void initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
