package com.xebia.xtime.monthoverview.loader;

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

public class MonthOverviewLoader extends AsyncTaskLoader<XTimeOverview> {

    private final Date mMonth;

    public MonthOverviewLoader(Context context, Date month) {
        super(context);
        mMonth = month;
    }

    @Override
    public XTimeOverview loadInBackground() {
        try {
            final String cookie = CookieHelper.getCookie(getContext());
            final String response = XTimeWebService.getInstance().getMonthOverview(mMonth, cookie);
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
