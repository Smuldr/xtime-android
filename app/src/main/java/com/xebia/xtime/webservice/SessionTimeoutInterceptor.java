package com.xebia.xtime.webservice;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Cookie;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpEngine;
import okio.Buffer;
import okio.BufferedSource;
import timber.log.Timber;

/**
 * HTTP interceptor that checks the response to see if XTime returns an error page
 */
public class SessionTimeoutInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();
        final Response response = chain.proceed(request);
        final ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        if (HttpEngine.hasBody(response)) {
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();

            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            if (contentLength != 0) {
                String content = buffer.clone().readString(charset);
                if (content.contains("UsernameNotFoundException")) {
                    String cookieHeader = request.header("Cookie");
                    if (null != cookieHeader) {
                        Cookie cookie = Cookie.parse(request.url(), cookieHeader);
                        Timber.w("Session '" + cookie + "' timed out");
                        throw new SessionExpiredException(cookie.value());
                    } else {
                        Timber.w("Request denied: no cookie in request");
                        throw new SessionExpiredException("");
                    }
                }
            }
        }
        return response;
    }
}
