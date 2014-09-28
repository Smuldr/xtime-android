package com.xebia.xtime.shared.webservice;

import com.squareup.okhttp.RequestBody;
import com.xebia.xtime.shared.model.Project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WorkTypesForProjectRequestBuilder {

    private String mWeekString;
    private String mProjectId;

    public WorkTypesForProjectRequestBuilder project(final Project project) {
        mProjectId = project.getId();
        return this;
    }

    public WorkTypesForProjectRequestBuilder week(final Date week) {
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
                "c0-methodName=getWorkTypesListForProjectInRange\n" +
                "c0-id=0\n" +
                "c0-param0=string:" + mProjectId + "\n" +
                "c0-param1=string:" + mWeekString + "\n" +
                "c0-param2=boolean:true\n" +
                "batchId=0";
        return RequestBody.create(MediaTypes.TEXT_PLAIN, body);
    }
}
