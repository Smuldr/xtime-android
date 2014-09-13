package com.xebia.xtime.weekoverview;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * PagerAdapter for switching between {@link DailyHoursListFragment}.
 * <p/>
 * After initialization the adapter should be set to START_INDEX. The page at START_INDEX
 * represents the current week, so you can move a couple of weeks back in time and also into the
 * future.
 */
public class WeekPagerAdapter extends FragmentPagerAdapter {

    public static final int START_INDEX = 4;
    public static final int COUNT = 8;
    private SparseArray<Fragment> mFragments;

    public WeekPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = mFragments.get(position);
        if (null == fragment) {
            Date date = getStartDate(position);
            fragment = DailyHoursListFragment.newInstance(date);
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
        Date startDate = getStartDate(position);
        return DailyHoursListFragment.getTitle(startDate);
    }

    private Date getStartDate(int index) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        calendar.add(Calendar.WEEK_OF_YEAR, index - START_INDEX);

        // roll back until monday
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_WEEK, -1);
        }

        // make sure the time is exactly 0:00
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        return calendar.getTime();
    }
}
