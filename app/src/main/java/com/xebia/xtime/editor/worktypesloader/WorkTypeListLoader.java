package com.xebia.xtime.editor.worktypesloader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.WorkType;

import org.apache.http.auth.AuthenticationException;

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
            String response = new WorkTypesForProjectRequest(mProject, mDate).submit();
            return WorkTypeListParser.parse(response);
        } catch (AuthenticationException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        // keep loading in the background
        forceLoad();
    }
}
