package com.xebia.xtime.monthoverview;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * PagerAdapter for switching between {@link com.xebia.xtime.monthoverview.MonthSummaryFragment}.
 * <p/>
 * After initialization the adapter should be set to START_INDEX. The page at START_INDEX
 * represents the current month, so you can move a couple of month back in time and also into the
 * future.
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

    public static final int START_INDEX = 11;
    public static final int COUNT = 13;
    private final DateFormat mDateFormat;
    private final SparseArray<Fragment> mFragments;

    public MonthPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>();
        mDateFormat = getDateFormat();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (null == fragment) {
            Date date = getMonth(position);
            fragment = MonthSummaryFragment.newInstance(date);
            mFragments.put(position, fragment);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mDateFormat.format(getMonth(position));
    }

    private Date getMonth(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        calendar.add(Calendar.MONTH, index - START_INDEX);

        // roll back until first day of the month
        while (calendar.get(Calendar.DAY_OF_MONTH) != 1) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        // make sure the time is exactly 0:00
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        return calendar.getTime();
    }

    @TargetApi(18)
    private DateFormat getDateFormat() {
        Locale locale = Locale.getDefault();
        String pattern = "MMMM yyyy";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, pattern);
        }
        DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
        return dateFormat;
    }
}
