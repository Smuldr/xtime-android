package com.xebia.xtime.editor.worktypesloader;

import android.accounts.AccountManager;
import android.content.AsyncTaskLoader;
import android.content.Context;

import com.xebia.xtime.authenticator.Authenticator;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.WorkType;
import com.xebia.xtime.webservice.SessionExpiredException;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class WorkTypeListLoader extends AsyncTaskLoader<List<WorkType>> {

    private final Project mProject;
    private final Date mDate;

    public WorkTypeListLoader(Context context, Project project, Date date) {
        super(context);
        mProject = project;
        mDate = date;
    }

    @Override
    public List<WorkType> loadInBackground() {
        try {
            return getWorkTypes();
        } catch (SessionExpiredException e) {
            AccountManager accountManager = AccountManager.get(getContext());
            accountManager.invalidateAuthToken(Authenticator.ACCOUNT_TYPE, e.getSessionId());
            return retry();
        } catch (IOException e) {
            return null;
        }
    }

    private List<WorkType> retry() {
        try {
            return getWorkTypes();
        } catch (IOException e) {
            Timber.e(e, "Retry failed");
            return null;
        }
    }

    private List<WorkType> getWorkTypes() throws IOException {
        String response = XTimeWebService.getInstance().getWorkTypesForProject(mProject, mDate);
        return WorkTypeListParser.parse(response);
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
