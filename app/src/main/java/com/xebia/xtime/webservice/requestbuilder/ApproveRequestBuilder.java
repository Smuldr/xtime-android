package com.xebia.xtime.webservice.requestbuilder;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.RequestBody;

public class ApproveRequestBuilder {

    private String mGrandTotalString;
    private String mMonthString;

    public ApproveRequestBuilder grandTotal(final double grandTotal) {
        mGrandTotalString = NumberFormat.getInstance(Locale.US).format(grandTotal);
        return this;
    }

    public ApproveRequestBuilder month(final Date month) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        mMonthString = dateFormat.format(month);
        return this;
    }

    public RequestBody build() {
        return RequestBody.create(MediaTypes.FORM_URLENCODED, "approvalMonthYear=" + mMonthString
                + "&approve=Approve" + "&inp_grandTotal=" + mGrandTotalString);
    }
}
