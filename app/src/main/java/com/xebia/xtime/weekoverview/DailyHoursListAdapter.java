package com.xebia.xtime.weekoverview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xebia.xtime.R;
import com.xebia.xtime.shared.model.DayOverview;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adapter that displays DayOverview objects in a list. Each list row contains a date indicator
 * and a view that displays the total number of work totalHours registered.
 */
public class DailyHoursListAdapter extends ArrayAdapter<DayOverview> {

    private final DateFormat mDateFormat;

    public DailyHoursListAdapter(Context context, List<DayOverview> data) {
        super(context, R.layout.row_daily_hours, R.id.hours, data);
        mDateFormat = getDateFormat();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // try to re-use the view instead of inflating a new one
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
            row = inflater.inflate(R.layout.row_daily_hours, parent, false);
        }

        if (row != null) {
            // find the view
            TextView dateView = (TextView) row.findViewById(R.id.date);
            TextView hoursView = (TextView) row.findViewById(R.id.hours);
            View approvedView = row.findViewById(R.id.approved);

            // update the view content
            DayOverview item = getItem(position);
            Date date = item.getDate();
            dateView.setText(mDateFormat.format(date));
            String totalHours = NumberFormat.getNumberInstance().format(item.getTotalHours());
            hoursView.setText(getContext().getString(R.string.hours_label, totalHours));
            approvedView.setVisibility(item.isEditable() ? View.GONE : View.VISIBLE);
            if (DateUtils.isToday(date.getTime())) {
                row.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.accent));
                dateView.setTextColor(Color.WHITE);
                hoursView.setTextColor(Color.WHITE);
            } else if (date.getTime() > new Date().getTime()) {
                row.setBackgroundColor(0);
                dateView.setTextColor(Color.GRAY);
                hoursView.setTextColor(Color.GRAY);
            }
        }

        return row;
    }

    @TargetApi(18)
    private DateFormat getDateFormat() {
        Locale locale = Locale.getDefault();
        String pattern = "EEE d MMMM";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, pattern);
        }
        DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        return dateFormat;
    }
}
