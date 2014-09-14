package com.xebia.xtime.monthoverview.approve;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.AsyncTask;

import com.xebia.xtime.shared.CookieHelper;
import com.xebia.xtime.shared.webservice.XTimeWebService;

import java.io.IOException;
import java.util.Date;

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
            String cookie = CookieHelper.getCookie(mContext);
            XTimeWebService.getInstance().approveMonth(hours, month, cookie);
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            return null;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onApproveComplete(result);
    }

    /**
     * Interface for listening for results from the SaveEntryTask
     */
    public interface Listener {
        public abstract void onApproveComplete(Boolean result);
    }

}
