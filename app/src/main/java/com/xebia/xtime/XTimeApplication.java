package com.xebia.xtime;

import android.app.Application;

import com.xebia.xtime.webservice.XTimeCookieJar;
import com.xebia.xtime.webservice.XTimeWebService;

import timber.log.Timber;

public class XTimeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
        initHttpClient();
    }

    private void initHttpClient() {
        Timber.d("Init HTTP client");
        XTimeWebService.init(new XTimeCookieJar(this));
    }

    private void initLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
