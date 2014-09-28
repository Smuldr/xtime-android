package com.xebia.xtime.webservice.requestbuilder;

import com.squareup.okhttp.RequestBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LoginRequestBuilder {

    private String mUsername;
    private String mPassword;

    public LoginRequestBuilder username(String username) {
        try {
            mUsername = URLEncoder.encode(username, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should not be possible
            throw new RuntimeException(e);
        }
        return this;
    }

    public LoginRequestBuilder password(String password) {
        try {
            mPassword = URLEncoder.encode(password, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should not be possible
            throw new RuntimeException(e);
        }
        return this;
    }

    public RequestBody build() {
        return RequestBody.create(MediaTypes.FORM_URLENCODED, "j_username=" + mUsername
                + "&j_password=" + mPassword);
    }
}
