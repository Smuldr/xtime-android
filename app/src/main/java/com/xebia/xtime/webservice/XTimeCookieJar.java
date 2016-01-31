package com.xebia.xtime.webservice;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import java.io.IOException;
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

    private static final String COOKIE_NAME = "JSESSIONID";
    private static final String XTIME_HOST = "xtime.xebia.com";
    private final Context context;

    public XTimeCookieJar(final Context context) {
        this.context = context;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        // nothing to do: we get the cookie from Android's AccountManager
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (isAuthenticatedXTimeApi(url)) {
            String sessionId;
            try {
                sessionId = SessionHelper.getSessionId(context);
            } catch (AuthenticatorException | OperationCanceledException | IOException e) {
                sessionId = null;
            }
            if (null == sessionId) {
                Timber.w("Could not get XTime cookie");
                return Collections.emptyList();
            }
            final Cookie cookie = new Cookie.Builder()
                    .name(COOKIE_NAME)
                    .value(sessionId)
                    .domain(XTIME_HOST)
                    .build();
            return Collections.singletonList(cookie);
        } else {
            Timber.d("No cookie for URL %s", url);
            return Collections.emptyList();
        }
    }

    private boolean isAuthenticatedXTimeApi(final HttpUrl url) {
        // do not try to get a session ID for non-XTime URLs or for login requests,
        // this causes an infinite loop of login requests
        return XTIME_HOST.equals(url.host())
                && !url.encodedPath().contains("j_spring_security_check")
                && !url.encodedPath().contains(";jsessionid=");
    }
}
