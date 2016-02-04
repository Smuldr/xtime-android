package com.xebia.xtime.dayoverview;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xebia.xtime.R;
import com.xebia.xtime.shared.model.TimeSheetEntry;

import java.text.NumberFormat;
import java.util.List;

public class DailyTimeSheetListAdapter extends ArrayAdapter<TimeSheetEntry> {

    public DailyTimeSheetListAdapter(Context context, List<TimeSheetEntry> objects) {
        super(context, R.layout.row_time_sheet_entry, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // try to re-use the view instead of inflating a new one
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.row_time_sheet_entry, parent, false);
        }

        // find the view
        if (row != null) {
            TextView projectView = (TextView) row.findViewById(R.id.project);
            TextView workTypeView = (TextView) row.findViewById(R.id.work_type);
            TextView descriptionView = (TextView) row.findViewById(R.id.description);
            TextView descriptionPlaceholder = (TextView) row.findViewById(R.id.description_placeholder);
            TextView hoursView = (TextView) row.findViewById(R.id.hours);

            // update the view content
            TimeSheetEntry item = getItem(position);
            projectView.setText(item.getProject().getName());
            workTypeView.setText(item.getWorkType().getDescription());
            descriptionView.setText(item.getDescription());
            String hours = NumberFormat.getNumberInstance().format(item.getTimeCell().getHours());
            hoursView.setText(getContext().getString(R.string.hours_label, hours));

            descriptionView.setText(item.getDescription());
            boolean hasDescription = TextUtils.isEmpty(item.getDescription());
            descriptionView.setVisibility(hasDescription ? View.INVISIBLE : View.VISIBLE);
            descriptionPlaceholder.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        }

        return row;
    }
}
