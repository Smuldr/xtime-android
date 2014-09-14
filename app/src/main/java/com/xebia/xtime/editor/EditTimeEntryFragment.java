package com.xebia.xtime.editor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
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
import com.xebia.xtime.editor.delete.DeleteEntryTask;
import com.xebia.xtime.editor.save.SaveEntryTask;
import com.xebia.xtime.editor.worktypesloader.WorkTypeListLoader;
import com.xebia.xtime.shared.model.Project;
import com.xebia.xtime.shared.model.Task;
import com.xebia.xtime.shared.model.TimeEntry;
import com.xebia.xtime.shared.model.WorkType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Fragment for a time sheet entry editor. Contains spinners for selecting the project and type
 * of work, and text boxes for the description and the amount of time to register.
 * <p>
 * If a time sheet entry is provided as an argument, only the time field is editable. When
 * creating a new entry, all fields are enabled.
 * </p>
 * <p>
 * The list of possible work types is fetched dynamically whenever a new project is selected,
 * using an AsyncTaskLoader. The list of projects is predefined.
 * </p>
 * <p>
 * The action bar contains an option to save the changes, which triggers an AsyncTask that sends
 * a {@link com.xebia.xtime.editor.save.SaveEntryRequest} to the XTime backend. When the task
 * finishes,
 * the parent activity is notified.
 * </p>
 */
public class EditTimeEntryFragment extends Fragment implements LoaderManager
        .LoaderCallbacks<List<WorkType>>, SaveEntryTask.Listener, DeleteEntryTask.Listener {

    private static final String ARG_PROJECTS = "projects";
    private static final String ARG_DATE = "date";
    private static final String ARG_TIME_SHEET = "time_cell";
    private static final String TAG = "EditTimeSheetFragment";
    /**
     * List of work type IDs where the description is required.
     */
    // TODO: Replace static list of description work types with data from XTime backend
    private static final List<String> REQUIRE_DESCR = Arrays.asList("960", "940", "920", "935");
    private Listener mListener;
    private TimeEntry mSaveEntry;
    private TimeEntry mTimeEntry;
    private List<Project> mProjects;
    private Date mDate;
    private List<WorkType> mWorkTypes;
    private View mBusyIndicatorView;
    private View mMainView;
    private Spinner mProjectView;
    private Spinner mWorkTypeView;
    private EditText mDescriptionView;
    private EditText mTimeView;

    public EditTimeEntryFragment() {
        // required empty constructor
    }

    /**
     * Factory method
     *
     * @param date     Date to edit/create TimeSheetEntry for
     * @param entry    (Optional) TimeSheetEntry to view/edit
     * @return Fragment
     */
    public static EditTimeEntryFragment getInstance(final Date date, final TimeEntry entry) {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, date.getTime());
        args.putParcelable(ARG_TIME_SHEET, entry);
        EditTimeEntryFragment fragment = new EditTimeEntryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != getArguments()) {
            mDate = new Date(getArguments().getLong(ARG_DATE));
            mProjects = getArguments().getParcelableArrayList(ARG_PROJECTS);
            mTimeEntry = getArguments().getParcelable(ARG_TIME_SHEET);
            mWorkTypes = new ArrayList<>();
        }

        if (null != mTimeEntry) {
            mWorkTypes.add(mTimeEntry.getTask().getWorkType());
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_time_entry, container, false);
        if (null == rootView) {
            return null;
        }

        // link the views
        mBusyIndicatorView = rootView.findViewById(R.id.busy_indicator);
        mMainView = rootView.findViewById(R.id.editor_form);
        mProjectView = (Spinner) rootView.findViewById(R.id.project);
        mWorkTypeView = (Spinner) rootView.findViewById(R.id.work_type);
        mDescriptionView = (EditText) rootView.findViewById(R.id.description);
        mTimeView = (EditText) rootView.findViewById(R.id.time);

        // set up the views
        initProjectView();
        initWorkTypeView();
        if (null != mTimeEntry) {
            mDescriptionView.setText(mTimeEntry.getTask().getDescription());
            mDescriptionView.setEnabled(false);
            mTimeView.setText(NumberFormat.getNumberInstance().format(mTimeEntry.getHours()));
        }

        return rootView;
    }

    private void initWorkTypeView() {
        // set up the spinner adapter
        ArrayAdapter<WorkType> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mWorkTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mWorkTypeView.setAdapter(adapter);

        // only enable the spinner if the work type is not already defined
        mWorkTypeView.setEnabled(null == mTimeEntry || null == mTimeEntry.getTask().getWorkType());
    }

    private void initProjectView() {
        // set up the spinner adapter
        ArrayAdapter<Project> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mProjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mProjectView.setAdapter(adapter);

        if (null != mTimeEntry && null != mTimeEntry.getTask().getProject()) {
            selectProject(mTimeEntry.getTask().getProject());
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
        getActivity().getLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_time_cell, menu);
        if (null == mTimeEntry) {
            // do not show delete button when creating new entries
            menu.removeItem(R.id.delete);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                onSaveClick();
                return true;
            case R.id.delete:
                onDeleteClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            showBusyIndicator(true);

            double time = getTimeInput();
            if (null == mTimeEntry) {
                // create new entry from the form
                Project project = (Project) mProjectView.getSelectedItem();
                WorkType workType = (WorkType) mWorkTypeView.getSelectedItem();
                String description = ("" + mDescriptionView.getText()).trim();
                Task task = new Task(project, workType, description);
                mSaveEntry = new TimeEntry(task, mDate, time, false);
                new SaveEntryTask(this).execute(mSaveEntry);
            } else {
                // only the time can be changed for existing time sheet entries
                mSaveEntry = new TimeEntry(mTimeEntry.getTask(), mTimeEntry.getEntryDate(), time,
                        mTimeEntry.isFromAfas());
                new SaveEntryTask(this).execute(mSaveEntry);
            }
        }
    }

    private void onDeleteClick() {
        showBusyIndicator(true);
        new DeleteEntryTask(this).execute(mTimeEntry);
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

    @Override
    public void onDeleteComplete(Boolean result) {
        if (null != result && result) {
            mListener.onEntryDelete(mTimeEntry);
        } else {
            showBusyIndicator(false);
            Toast.makeText(getActivity(), R.string.toast_delete_fail, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveComplete(Boolean result) {
        if (null != result && result) {
            mListener.onEntryUpdate(mSaveEntry);
        } else {
            showBusyIndicator(false);
            Toast.makeText(getActivity(), R.string.toast_save_fail, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Shows the busy indicator UI and hides the editor form.
     */
    @SuppressWarnings("ConstantConditions")
    private void showBusyIndicator(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mBusyIndicatorView.setVisibility(View.VISIBLE);
        mBusyIndicatorView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mBusyIndicatorView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });

        mMainView.setVisibility(View.VISIBLE);
        mMainView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
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

    /**
     * Interface for listening to events from a EditTimeSheetFragment.
     */
    public interface Listener {
        /**
         * @param entry The entry that was updated.
         */
        public abstract void onEntryUpdate(TimeEntry entry);

        /**
         * @param entry The entry that was deleted.
         */
        public abstract void onEntryDelete(TimeEntry entry);
    }
}
