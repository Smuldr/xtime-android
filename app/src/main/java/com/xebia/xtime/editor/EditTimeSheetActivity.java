package com.xebia.xtime.editor;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.xebia.xtime.R;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeSheetEntry;

import java.util.ArrayList;
import java.util.Date;

/**
 * Activity that wraps a {@link EditTimeSheetFragment}.
 * <p/>
 * When the activity is created, it checks the {@link #EXTRA_TIME_SHEET} Intent extra for a time
 * sheet entry to edit. If the time sheet is not present, a new one will be created.
 */
public class EditTimeSheetActivity extends Activity implements EditTimeSheetFragment
        .Listener {

    /**
     * Key for intent extra that contains the list of projects
     */
    public static final String EXTRA_PROJECTS = "projects";
    /**
     * Key for intent extra that contains time stamp of the day
     */
    public static final String EXTRA_DATE = "date";
    /**
     * Key for (optional) intent extra that contains the TimeSheetEntry to edit,
     * leave null to create a new entry
     */
    public static final String EXTRA_TIME_SHEET = "time_sheet";
    /**
     * Result code for when the entry was deleted
     */
    public static final int RESULT_DELETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_container);

        Date date = new Date(getIntent().getLongExtra(EXTRA_DATE, -1));
        if (date.getTime() < 0) {
            throw new NullPointerException("Missing EXTRA_DATE");
        }

        ArrayList<Project> projects = getIntent().getParcelableArrayListExtra(EXTRA_PROJECTS);
        if (null == projects) {
            throw new NullPointerException("Missing EXTRA_PROJECTS");
        }

        TimeSheetEntry entry = getIntent().getParcelableExtra(EXTRA_TIME_SHEET);

        if (savedInstanceState == null) {
            Fragment fragment = EditTimeSheetFragment.getInstance(date, projects, entry);
            getFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        }
    }

    @Override
    public void onEntryUpdate(TimeSheetEntry entry) {
        // put the time sheet in the result data
        Intent data = new Intent();
        data.putExtra(EXTRA_TIME_SHEET, entry);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onEntryDelete(TimeSheetEntry entry) {
        // put the time sheet in the result data
        Intent data = new Intent();
        data.putExtra(EXTRA_TIME_SHEET, entry);
        setResult(RESULT_DELETE, data);
        finish();
    }
}
