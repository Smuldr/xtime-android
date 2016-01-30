package com.xebia.xtime.webservice;

import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import timber.log.Timber;

/**
 * Cookie jar that only stores the XTime authentication cookie
 */
public class XTimeCookieJar implements CookieJar {

    private final String XTIME_HOST = "xtime.xebia.com";
    private Cookie xTimeCookie = null;

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (XTIME_HOST.equals(url.host())) {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.name())) {
                    Timber.d("Found XTime cookie %s=%s", cookie.name(), cookie.value());
                    xTimeCookie = cookie;
                    return;
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (XTIME_HOST.equals(url.host()) && xTimeCookie != null) {
            return Collections.singletonList(xTimeCookie);
        } else {
            return Collections.emptyList();
        }
    }
}
