package com.xebia.xtime.editor;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.xebia.xtime.R;
import com.xebia.xtime.editor.worktypesloader.WorkTypeListLoader;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.TimeCell;
import com.xebia.xtime.shared.model.TimeSheetEntry;
import com.xebia.xtime.shared.model.WorkType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EditTimeSheetFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<List<WorkType>> {

    private static final String ARG_PROJECTS = "projects";
    private static final String ARG_DATE = "date";
    private static final String ARG_TIME_SHEET = "time_sheet";
    private static final String TAG = "EditTimeSheetFragment";
    /**
     * List of work type IDs where the description is required.
     */
    // TODO: Replace static list of description work types with data from XTime backend
    private static final List<String> REQUIRE_DESCR = Arrays.asList("960", "940", "920", "935");
    private TimeSheetEntry mTimeSheetEntry;
    private List<Project> mProjects;
    private Date mDate;
    private List<WorkType> mWorkTypes;
    private Spinner mProjectView;
    private Spinner mWorkTypeView;
    private EditText mDescriptionView;
    private EditText mTimeView;
    private Listener mListener;

    public EditTimeSheetFragment() {
        // required empty constructor
    }

    /**
     * Factory method
     *
     * @param date     Date to edit/create TimeSheetEntry for
     * @param projects List of available projects on this data
     * @param entry    (Optional) TimeSheetEntry to view/edit
     * @return Fragment
     */
    public static EditTimeSheetFragment getInstance(Date date, ArrayList<Project> projects,
                                                    TimeSheetEntry entry) {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, date.getTime());
        args.putParcelableArrayList(ARG_PROJECTS, projects);
        args.putParcelable(ARG_TIME_SHEET, entry);
        EditTimeSheetFragment fragment = new EditTimeSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != getArguments()) {
            mDate = new Date(getArguments().getLong(ARG_DATE));
            mProjects = getArguments().getParcelableArrayList(ARG_PROJECTS);
            mTimeSheetEntry = getArguments().getParcelable(ARG_TIME_SHEET);
            mWorkTypes = new ArrayList<WorkType>();
        }

        if (null != mTimeSheetEntry) {
            mWorkTypes.add(mTimeSheetEntry.getWorkType());
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_time_cell, container, false);
        if (null == rootView) {
            return null;
        }

        // link the views
        mProjectView = (Spinner) rootView.findViewById(R.id.project);
        mWorkTypeView = (Spinner) rootView.findViewById(R.id.work_type);
        mDescriptionView = (EditText) rootView.findViewById(R.id.description);
        mTimeView = (EditText) rootView.findViewById(R.id.time);

        // set up the views
        initProjectView();
        initWorkTypeView();
        if (null != mTimeSheetEntry) {
            mDescriptionView.setText(mTimeSheetEntry.getDescription());
            mDescriptionView.setEnabled(false);
            mTimeView.setText(NumberFormat.getNumberInstance()
                    .format(mTimeSheetEntry.getTimeCell().getHours()));
        }

        return rootView;
    }

    private void initWorkTypeView() {
        // set up the spinner adapter
        ArrayAdapter<WorkType> adapter = new ArrayAdapter<WorkType>(getActivity(),
                android.R.layout.simple_spinner_item, mWorkTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWorkTypeView.setAdapter(adapter);

        // only enable the spinner if the work type is not already defined
        mWorkTypeView.setEnabled(null == mTimeSheetEntry || null == mTimeSheetEntry.getWorkType());
    }

    private void initProjectView() {
        // set up the spinner adapter
        ArrayAdapter<Project> adapter = new ArrayAdapter<Project>(getActivity(),
                android.R.layout.simple_spinner_item, mProjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProjectView.setAdapter(adapter);

        if (null != mTimeSheetEntry && null != mTimeSheetEntry.getProject()) {
            selectProject(mTimeSheetEntry.getProject());
            mProjectView.setEnabled(false);
        } else {
            listenProjectSelection();
        }
    }

    /**
     * Starts listening for item selection events from the projects spinner. Triggers a work type
     * list load whenever the project selection changes.
     */
    private void listenProjectSelection() {
        mProjectView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                Project project = (Project) mProjectView.getItemAtPosition(pos);
                loadWorkTypes(project);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mWorkTypes.clear();
                ((ArrayAdapter) mWorkTypeView.getAdapter()).notifyDataSetChanged();
            }
        });
    }

    private void selectProject(Project project) {
        // compare projects on project ID, somehow the project description is not constant
        // e.g. 'Internal projects' vs. 'Internal Project'
        int index = -1;
        for (int i = 0; i < mProjects.size(); i++) {
            if (mProjects.get(i).getId().equals(project.getId())) {
                index = i;
                break;
            }
        }

        // select the correct item
        if (index >= 0) {
            mProjectView.setSelection(index);
        }
    }

    private void loadWorkTypes(Project project) {
        Bundle args = new Bundle();
        args.putParcelable("project", project);
        args.putLong("date", mDate.getTime());
        getActivity().getSupportLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_time_cell, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            onSaveClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "EditTimeSheetFragment.Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<WorkType>> onCreateLoader(int id, Bundle args) {
        Project project = args.getParcelable("project");
        Date date = new Date(args.getLong("date"));
        return new WorkTypeListLoader(getActivity(), project, date);
    }

    @Override
    public void onLoadFinished(Loader<List<WorkType>> listLoader, List<WorkType> workTypes) {
        if (null == workTypes) {
            Toast.makeText(getActivity(), R.string.toast_work_types_fail, Toast.LENGTH_LONG).show();
            return;
        }

        mWorkTypes.clear();
        mWorkTypes.addAll(workTypes);
        ((ArrayAdapter) mWorkTypeView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<WorkType>> listLoader) {
        mWorkTypes.clear();
    }

    private boolean validateForm() {

        View focusView = null;
        int errorResId = -1;

        if (TextUtils.isEmpty(mTimeView.getText())) {
            focusView = mTimeView;
            errorResId = R.string.error_field_required;
        } else {
            double time = getTimeInput();
            if (time < 0) {
                focusView = mTimeView;
                errorResId = R.string.error_invalid_time;
            }
        }

        WorkType workType = (WorkType) mWorkTypeView.getSelectedItem();
        if (null == workType) {
            focusView = mWorkTypeView;
        } else {
            if (REQUIRE_DESCR.contains(workType.getId()) && TextUtils.isEmpty(mDescriptionView
                    .getText())) {
                focusView = mDescriptionView;
                errorResId = R.string.error_field_required;
            }
        }

        Project project = (Project) mProjectView.getSelectedItem();
        if (null == project) {
            focusView = mProjectView;
        }

        if (null != focusView && errorResId > 0) {
            if (focusView instanceof EditText) {
                ((EditText) focusView).setError(getActivity().getString(errorResId));
            }
            focusView.requestFocus();
        }

        return null == focusView && errorResId < 0;
    }

    private void onSaveClick() {
        if (validateForm()) {
            double time = getTimeInput();
            if (null == mTimeSheetEntry) {
                Project project = (Project) mProjectView.getSelectedItem();
                WorkType workType = (WorkType) mWorkTypeView.getSelectedItem();
                String descr = "" + mDescriptionView.getText();
                TimeCell timeCell = new TimeCell(mDate, time, false, false, false);
                TimeSheetEntry newEntry = new TimeSheetEntry(project, workType, descr, timeCell);
                new SaveTask().execute(newEntry);
            } else {
                // only the time can be changed for existing time sheet entries
                mTimeSheetEntry.getTimeCell().setHours(time);
                new SaveTask().execute(mTimeSheetEntry);
            }
        }
    }

    private double getTimeInput() {
        double time = -1;
        String timeString = mTimeView.getText() + "";
        try {
            time = Double.parseDouble(timeString);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse time input: " + mTimeView.getText());
        }
        return time;
    }

    /**
     * Interface for listening to events from a EditTimeSheetFragment.
     */
    public interface Listener {
        public abstract void onChangesSaved();
    }

    /**
     * Asynchronous task to save the changes to this time cell.
     */
    private class SaveTask extends AsyncTask<TimeSheetEntry, Void, Boolean> {

        @Override
        protected Boolean doInBackground(TimeSheetEntry... params) {
            if (null == params || params.length < 1) {
                throw new NullPointerException("Missing TimeSheetEntry parameter");
            }
            TimeSheetEntry entry = params[0];
            return new SaveTimeSheetRequest(entry).submit();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (null != result && result) {
                mListener.onChangesSaved();
            } else {
                Toast.makeText(getActivity(), R.string.toast_save_fail, Toast.LENGTH_LONG).show();
            }
        }
    }
}
