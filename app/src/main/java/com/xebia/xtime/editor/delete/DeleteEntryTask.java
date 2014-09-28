package com.xebia.xtime.editor.delete;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.AsyncTask;

import com.xebia.xtime.shared.CookieHelper;
import com.xebia.xtime.shared.model.TimeSheetEntry;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;

/**
 * Asynchronous task to delete a time sheet entry
 */
public class DeleteEntryTask extends AsyncTask<TimeSheetEntry, Void, Boolean> {

    private final Context mContext;
    private final Listener mListener;

    public DeleteEntryTask(final Context context, final Listener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(TimeSheetEntry... params) {
        if (null == params || params.length < 1) {
            throw new NullPointerException("Missing TimeSheetEntry parameter");
        }

        try {
            String cookie = CookieHelper.getCookie(mContext);
            String response = XTimeWebService.getInstance()
                    .deleteEntry(params[0], cookie);
            return DeleteEntryResponseParser.parse(response);
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mListener.onDeleteComplete(result);
    }

    /**
     * Interface for listening for results from the DeleteEntryTask
     */
    public interface Listener {
        public abstract void onDeleteComplete(Boolean result);
    }
}
