package com.xebia.xtime.test.webservice;

import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * Cookie jar that adds a JSESSIONID cookie to every request
 */
public class MockCookieJar implements CookieJar {

    public static final String COOKIE_VALUE = "JSESSIONID=C0FFEE";

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // nothing to do
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return Collections.singletonList(new Cookie.Builder()
                .name("JSESSIONID")
                .value("C0FFEE")
                .domain(url.host())
                .build());
    }
}
