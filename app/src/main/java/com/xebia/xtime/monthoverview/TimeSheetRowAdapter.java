package com.xebia.xtime.monthoverview;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xebia.xtime.R;
import com.xebia.xtime.shared.TimeSheetUtils;
import com.xebia.xtime.shared.model.TimeSheetRow;

import java.text.NumberFormat;
import java.util.List;

/**
 * List adapter that takes a list of time sheet rows and shows a summary of each row in the view.
 * <p/>
 * The summary contains the project name, work type and optional description,
 * along with the total number of hours that were spent on the project.
 */
public class TimeSheetRowAdapter extends ArrayAdapter<TimeSheetRow> {

    public TimeSheetRowAdapter(Context context, List<TimeSheetRow> objects) {
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

        // update the view content
        TimeSheetRow item = getItem(position);
        if (row != null) {
            TextView projectView = (TextView) row.findViewById(R.id.project);
            projectView.setText(item.getProject().getName());

            TextView workTypeView = (TextView) row.findViewById(R.id.work_type);
            workTypeView.setText(item.getWorkType().getDescription());

            TextView descriptionView = (TextView) row.findViewById(R.id.description);
            TextView descriptionPlaceholder = (TextView) row.findViewById(R.id.description_placeholder);
            descriptionView.setText(item.getDescription());
            boolean hasDescription = TextUtils.isEmpty(item.getDescription());
            descriptionView.setVisibility(hasDescription ? View.INVISIBLE : View.VISIBLE);
            descriptionPlaceholder.setVisibility(hasDescription ? View.VISIBLE : View.GONE);


            TextView hoursView = (TextView) row.findViewById(R.id.hours);
            double hours = TimeSheetUtils.getTotalHours(item);
            String formattedHours = NumberFormat.getNumberInstance().format(hours);
            hoursView.setText(getContext().getString(R.string.hours_label, formattedHours));
        }

        return row;
    }
}
