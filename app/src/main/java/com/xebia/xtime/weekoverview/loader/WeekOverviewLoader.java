package com.xebia.xtime.weekoverview.loader;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.xebia.xtime.shared.CookieHelper;
import com.xebia.xtime.shared.model.XTimeOverview;
import com.xebia.xtime.shared.parser.XTimeOverviewParser;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;
import java.util.Date;

import timber.log.Timber;

public class WeekOverviewLoader extends AsyncTaskLoader<XTimeOverview> {

    private final Date mDate;

    public WeekOverviewLoader(Context context, Date date) {
        super(context);
        mDate = date;
    }

    @Override
    public XTimeOverview loadInBackground() {
        try {
            final String cookie = CookieHelper.getCookie(getContext());
            final String response = XTimeWebService.getInstance().getWeekOverview(mDate, cookie);
            return XTimeOverviewParser.parse(response);
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            Timber.e(e, "Authentication error");
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
