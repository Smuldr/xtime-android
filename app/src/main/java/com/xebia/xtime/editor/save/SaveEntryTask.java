package com.xebia.xtime.editor.save;

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
 * Asynchronous task to save the changes to a time sheet entry
 */
public class SaveEntryTask extends AsyncTask<TimeSheetEntry, Void, Boolean> {

    private final Context context;
    private final Listener listener;

    public SaveEntryTask(final Context context, final Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(TimeSheetEntry... params) {
        if (null == params || params.length < 1) {
            throw new NullPointerException("Missing TimeSheetEntry parameter");
        }
        try {
            return XTimeWebService.getInstance().saveEntry(params[0]);
        } catch (SessionExpiredException e) {
            AccountManager accountManager = AccountManager.get(context);
            accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, e.getSessionId());
            return retry(params[0]);
        } catch (IOException e) {
            return null;
        }
    }

    public Boolean retry(final TimeSheetEntry entry) {
        try {
            return XTimeWebService.getInstance().saveEntry(entry);
        } catch (IOException e) {
            Timber.e(e, "Retry failed");
            return null;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        listener.onSaveComplete(result);
    }

    /**
     * Interface for listening for results from the SaveEntryTask
     */
    public interface Listener {
        void onSaveComplete(Boolean result);
    }
}
