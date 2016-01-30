package com.xebia.xtime.webservice.requestbuilder;

import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.WorkType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.RequestBody;

public class DeleteEntryRequestBuilder {

    private String mProjectId;
    private String mWorkTypeId;
    private String mDescription;
    private String mDateString;

    public DeleteEntryRequestBuilder project(final Project project) {
        mProjectId = project.getId();
        return this;
    }

    public DeleteEntryRequestBuilder workType(final WorkType workType) {
        mWorkTypeId = workType.getId();
        return this;
    }

    public DeleteEntryRequestBuilder description(final String description) {
        mDescription = description;
        return this;
    }

    public DeleteEntryRequestBuilder entryDate(final Date entryDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        mDateString = dateFormat.format(entryDate);
        return this;
    }

    public RequestBody build() {
        final String body = "callCount=1\n"
                + "page=/xtime/entryform.html\n"
                + "httpSessionId=\n"
                + "scriptSessionId=\n"
                + "c0-scriptName=TimeEntryServiceBean\n"
                + "c0-methodName=deleteTimeSheetEntries\n"
                + "c0-id=0\n"
                + "c0-param0=string:" + mProjectId + "\n"
                + "c0-param1=string:" + mWorkTypeId + "\n"
                + "c0-param2=string:" + mDescription + "\n"
                + "c0-param3=string:" + mDateString + "\n"
                + "batchId=0";
        return RequestBody.create(MediaTypes.TEXT_PLAIN, body);
    }
}
