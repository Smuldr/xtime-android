package com.xebia.xtime.dayoverview;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.xebia.xtime.R;
import com.xebia.xtime.shared.model.TimeSheetEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of {@link TimeSheetEntry} in a ListView.
 * <p/>
 * Each row displays the details of the time sheet entry (project, work type,
 * hours registered). Clicking on an item should open up an editor.
 */
public class DailyTimeSheetFragment extends ListFragment {

    private static final String ARG_TIME_SHEETS = "time_sheets";
    private List<TimeSheetEntry> mTimeSheetEntries;
    private Listener mListener;

    public DailyTimeSheetFragment() {
        // Required empty public constructor
    }

    public static DailyTimeSheetFragment getInstance(ArrayList<TimeSheetEntry> timeSheetEntries) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_TIME_SHEETS, timeSheetEntries);
        DailyTimeSheetFragment fragment = new DailyTimeSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Callback to refresh the list view when the list of time sheets has been changed
     */
    public void onDataSetChanged() {
        ((ArrayAdapter) getListView().getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (null != getArguments()) {
            mTimeSheetEntries = getArguments().getParcelableArrayList(ARG_TIME_SHEETS);
        }
        if (null == mTimeSheetEntries) {
            throw new NullPointerException("Missing ARG_TIME_SHEETS argument");
        }

        DailyTimeSheetListAdapter adapter = new DailyTimeSheetListAdapter(getActivity(),
                mTimeSheetEntries);
        setListAdapter(adapter);

        setEmptyText(getActivity().getString(R.string.empty_day_overview));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        TimeSheetEntry item = (TimeSheetEntry) l.getItemAtPosition(position);
        mListener.onTimeSheetEntrySelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement DailyTimeSheetFragment.Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Interface for handling clicks on the list of TimeSheetEntries
     */
    public interface Listener {
        void onTimeSheetEntrySelected(TimeSheetEntry selected);
    }
}
