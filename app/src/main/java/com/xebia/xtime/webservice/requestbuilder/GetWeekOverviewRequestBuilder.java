package com.xebia.xtime.webservice.requestbuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.RequestBody;

public class GetWeekOverviewRequestBuilder {

    private String mWeekString;

    public GetWeekOverviewRequestBuilder week(final Date week) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        mWeekString = dateFormat.format(week);
        return this;
    }

    public RequestBody build() {
        final String body = "callCount=1\n" +
                "page=/xtime/entryform.html\n" +
                "httpSessionId=\n" +
                "scriptSessionId=\n" +
                "c0-scriptName=TimeEntryServiceBean\n" +
                "c0-methodName=getWeekOverview\n" +
                "c0-id=0\n" + // only used for JSONP callback
                "c0-param0=string:" + mWeekString + "\n" +
                "c0-param1=boolean:true\n" + // not used?
                "batchId=0\n"; // only used for JSONP callback
        return RequestBody.create(MediaTypes.TEXT_PLAIN, body);
    }
}
