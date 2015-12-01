package com.xebia.xtime.weekoverview.loader;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;

import com.xebia.xtime.shared.XTimeAuthenticationException;
import com.xebia.xtime.shared.model.XTimeOverview;
import com.xebia.xtime.shared.parser.XTimeOverviewParser;

import java.util.Date;

public class WeekOverviewLoader extends AsyncTaskLoader<XTimeOverview> {

    private final Date mDate;

    public WeekOverviewLoader(Context context, Date date) {
        super(context);
        mDate = date;
    }

    @Override
    public XTimeOverview loadInBackground() {
        try {
            String response = new WeekOverviewRequest(mDate).submit();
            return XTimeOverviewParser.parse(response);
        } catch (XTimeAuthenticationException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
