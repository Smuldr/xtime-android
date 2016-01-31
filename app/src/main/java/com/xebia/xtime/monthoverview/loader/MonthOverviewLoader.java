package com.xebia.xtime.monthoverview.loader;

import android.accounts.AccountManager;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.xebia.xtime.authenticator.Authenticator;
import com.xebia.xtime.shared.model.XTimeOverview;
import com.xebia.xtime.shared.parser.XTimeOverviewParser;
import com.xebia.xtime.webservice.SessionExpiredException;
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
            return getOverview();
        } catch (SessionExpiredException e) {
            AccountManager accountManager = AccountManager.get(getContext());
            accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, e.getSessionId());
            return retry();
        } catch (IOException e) {
            return null;
        }
    }

    private XTimeOverview getOverview() throws IOException {
        final String response = XTimeWebService.getInstance().getMonthOverview(mMonth);
        return XTimeOverviewParser.parse(response);
    }

    private XTimeOverview retry() {
        try {
            return getOverview();
        } catch (IOException e) {
            Timber.e(e, "Retry failed");
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
