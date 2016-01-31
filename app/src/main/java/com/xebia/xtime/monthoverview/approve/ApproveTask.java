package com.xebia.xtime.monthoverview.approve;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;

import com.xebia.xtime.authenticator.Authenticator;
import com.xebia.xtime.webservice.SessionExpiredException;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;
import java.util.Date;

import timber.log.Timber;

/**
 * Asynchronous task to submit a request to approve the data for a month.
 */
public class ApproveTask extends AsyncTask<Double, Void, Boolean> {

    private final Context mContext;
    private final Listener mListener;

    public ApproveTask(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Double... params) {
        if (null == params || params.length < 2) {
            throw new NullPointerException("Missing month or hours parameter");
        }
        double hours = params[0];
        Date month = new Date(Math.round(params[1]));

        try {
            return approve(hours, month);
        } catch (SessionExpiredException e) {
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, e.getSessionId());
            return retry(hours, month);
        } catch (IOException e) {
            return null;
        }
    }

    private Boolean approve(final double hours, final Date month) throws IOException {
        return XTimeWebService.getInstance().approveMonth(hours, month);
    }

    private Boolean retry(double hours, Date month) {
        try {
            return approve(hours, month);
        } catch (IOException e) {
            Timber.e(e, "Retry failed");
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onApproveComplete(result);
    }

    /**
     * Interface for listening for results from the SaveEntryTask
     */
    public interface Listener {
        void onApproveComplete(Boolean result);
    }
}
