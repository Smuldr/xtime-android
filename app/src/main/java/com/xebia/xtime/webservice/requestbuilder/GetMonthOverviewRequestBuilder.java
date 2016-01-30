package com.xebia.xtime.webservice.requestbuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.RequestBody;

public class GetMonthOverviewRequestBuilder {

    private String mMonthString;

    public GetMonthOverviewRequestBuilder month(final Date month) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        mMonthString = dateFormat.format(month);
        return this;
    }

    public RequestBody build() {
        final String body = "callCount=1\n" +
                "page=/xtime/monthlyApprove.html\n" +
                "httpSessionId=\n" +
                "scriptSessionId=\n" +
                "c0-scriptName=TimeEntryServiceBean\n" +
                "c0-methodName=getMonthOverview\n" +
                "c0-id=0\n" +
                "c0-param0=string:" + mMonthString + "\n" +
                "batchId=0";
        return RequestBody.create(MediaTypes.TEXT_PLAIN, body);
    }
}
