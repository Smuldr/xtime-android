package com.xebia.xtime.editor.delete;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;

import com.xebia.xtime.authenticator.Authenticator;
import com.xebia.xtime.shared.model.TimeSheetEntry;
import com.xebia.xtime.webservice.SessionExpiredException;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;

import timber.log.Timber;

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
            return deleteEntry(params[0]);
        } catch (SessionExpiredException e) {
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, e.getSessionId());
            return retry(params[0]);
        } catch (IOException e) {
            return null;
        }
    }

    private Boolean deleteEntry(final TimeSheetEntry entry) throws IOException {
        final String response = XTimeWebService.getInstance().deleteEntry(entry);
        return DeleteEntryResponseParser.parse(response);
    }

    private Boolean retry(final TimeSheetEntry entry) {
        try {
            return deleteEntry(entry);
        } catch (IOException e) {
            Timber.e(e, "Retry failed");
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
        void onDeleteComplete(Boolean result);
    }
}
