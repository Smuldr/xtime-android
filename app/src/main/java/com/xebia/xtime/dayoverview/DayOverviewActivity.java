package com.xebia.xtime.dayoverview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.xebia.xtime.R;
import com.xebia.xtime.editor.EditTimeSheetActivity;
import com.xebia.xtime.shared.model.DayOverview;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeSheetEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Activity that displays a list of {@link TimeSheetEntry} in a {@link DailyTimeSheetFragment}.
 * <p/>
 * Clicking on a time sheet entry opens up the {@link EditTimeSheetActivity},
 * and the action bar also contains an option to create a new time sheet entry.
 */
public class DayOverviewActivity extends AppCompatActivity implements DailyTimeSheetFragment.Listener {

    /**
     * Key for intent extra that contains the day overview to display
     */
    public static final String EXTRA_DAY_OVERVIEW = "day_overview";
    private static final int REQ_CODE_EDIT = 1;
    private static final int REQ_CODE_CREATE = 2;
    private DayOverview mOverview;
    private TimeSheetEntry mSelectedEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_container);

        // get the day overview
        mOverview = getIntent().getParcelableExtra(EXTRA_DAY_OVERVIEW);
        if (null == mOverview) {
            throw new NullPointerException("Missing EXTRA_DAY_OVERVIEW");
        }

        // set up the UI
        if (savedInstanceState == null) {
            ArrayList<TimeSheetEntry> timeSheets = (ArrayList<TimeSheetEntry>) mOverview
                    .getTimeSheetEntries();
            Fragment fragment = DailyTimeSheetFragment.getInstance(timeSheets);
            FragmentTransaction tx = getFragmentManager().beginTransaction();
            tx.replace(R.id.content, fragment, "tag");
            tx.commit();
        }

        // set up the title
        setTitle(getDayTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mOverview.isEditable()) {
            getMenuInflater().inflate(R.menu.menu_day_overview, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.newItem) {
            startEditor(null, REQ_CODE_CREATE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTimeSheetEntrySelected(TimeSheetEntry selected) {
        if (mOverview.isEditable()) {
            mSelectedEntry = selected;
            startEditor(selected, REQ_CODE_EDIT);
        }
    }

    private void startEditor(TimeSheetEntry entry, int requestCode) {
        Intent editor = new Intent(this, EditTimeSheetActivity.class);
        editor.putExtra(EditTimeSheetActivity.EXTRA_DATE, mOverview.getDate().getTime());
        editor.putParcelableArrayListExtra(EditTimeSheetActivity.EXTRA_PROJECTS,
                (ArrayList<Project>) mOverview.getProjects());
        editor.putExtra(EditTimeSheetActivity.EXTRA_TIME_SHEET, entry);
        startActivityForResult(editor, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQ_CODE_EDIT:
                if (RESULT_OK == resultCode) {
                    TimeSheetEntry edited = data.getParcelableExtra(EditTimeSheetActivity
                            .EXTRA_TIME_SHEET);
                    onEntryEdited(edited);
                } else if (EditTimeSheetActivity.RESULT_DELETE == resultCode) {
                    TimeSheetEntry deleted = data.getParcelableExtra(EditTimeSheetActivity
                            .EXTRA_TIME_SHEET);
                    onEntryDeleted(deleted);
                }
                mSelectedEntry = null;
                break;
            case REQ_CODE_CREATE:
                if (RESULT_OK == resultCode) {
                    TimeSheetEntry created = data.getParcelableExtra(EditTimeSheetActivity
                            .EXTRA_TIME_SHEET);
                    onEntryCreated(created);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onEntryDeleted(TimeSheetEntry deleted) {
        int index = mOverview.getTimeSheetEntries().indexOf(deleted);
        mOverview.getTimeSheetEntries().remove(index);
        onDataSetChanged();
    }

    private void onEntryEdited(TimeSheetEntry edited) {
        mSelectedEntry.getTimeCell().setHours(edited.getTimeCell().getHours());
        onDataSetChanged();
    }

    private void onEntryCreated(TimeSheetEntry created) {
        mOverview.getTimeSheetEntries().add(created);
        onDataSetChanged();
    }

    @TargetApi(18)
    private String getDayTitle() {
        Locale locale = Locale.getDefault();
        String pattern = "EEEE d MMMM";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, pattern);
        }
        DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        return dateFormat.format(mOverview.getDate());
    }

    /**
     * Notifies the list fragment that the data set changed and the list might have to be updated.
     */
    private void onDataSetChanged() {
        DailyTimeSheetFragment fragment = (DailyTimeSheetFragment) getFragmentManager()
                .findFragmentByTag("tag");
        fragment.onDataSetChanged();
    }
}
