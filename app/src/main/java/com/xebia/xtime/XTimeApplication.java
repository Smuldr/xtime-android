package com.xebia.xtime;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.xebia.xtime.webservice.XTimeCookieJar;
import com.xebia.xtime.webservice.XTimeWebService;

import timber.log.Timber;

public class XTimeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogging();
        initHttpClient();
        cleanOldPrefs();
    }

    /**
     * Cleans up any old login credentials
     */
    private void cleanOldPrefs() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.contains("com.xebia.xtime.extra.PASSWORD")) {
            Timber.d("Clean up old preferences");
            preferences.edit()
                    .remove("com.xebia.xtime.extra.USERNAME")
                    .remove("com.xebia.xtime.extra.PASSWORD")
                    .apply();
        }
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
