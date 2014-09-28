package com.xebia.xtime.editor.worktypesloader;

import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AsyncTaskLoader;
import android.content.Context;

import com.xebia.xtime.shared.CookieHelper;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.WorkType;
import com.xebia.xtime.shared.webservice.XTimeWebService;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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
            String cookie = CookieHelper.getCookie(getContext());
            String response = XTimeWebService.getInstance()
                    .getWorkTypesForProject(mProject, mDate, cookie);
            return WorkTypeListParser.parse(response);
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
