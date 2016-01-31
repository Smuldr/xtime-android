package com.xebia.xtime.webservice;

import android.text.TextUtils;

import java.io.IOException;

public class SessionExpiredException extends IOException {

    private final String sessionId;

    public SessionExpiredException(final String sessionId) {
        super(TextUtils.isEmpty(sessionId)
                ? "No active session"
                : "Session '" + sessionId + "' expired");
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
