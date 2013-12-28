package com.xebia.xtime.weekoverview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.xebia.xtime.R;
import com.xebia.xtime.dayoverview.DayOverviewActivity;
import com.xebia.xtime.login.LoginActivity;
import com.xebia.xtime.shared.model.DayOverview;
import com.xebia.xtime.shared.model.WeekOverview;

public class WeekOverviewActivity extends ActionBarActivity implements DailyHoursListFragment
        .DailyHoursListener {

    private static final int REQ_CODE_LOGIN = 1;
    private static final String KEY_LOGGED_IN = "logged_in";
    private boolean mLoggedIn;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_LOGIN) {
            mLoggedIn = resultCode == RESULT_OK;
            if (!isLoggedIn()) {
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState) {
            mLoggedIn = savedInstanceState.getBoolean(KEY_LOGGED_IN, false);
        }

        if (!isLoggedIn()) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivityForResult(login, REQ_CODE_LOGIN);
        }

        setContentView(R.layout.activity_week_overview);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new WeekPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(WeekPagerAdapter.START_INDEX);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_LOGGED_IN, mLoggedIn);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClicked(WeekOverview weekOverview, DayOverview dayOverview) {
        Intent intent = new Intent(this, DayOverviewActivity.class);
        intent.putExtra(DayOverviewActivity.EXTRA_DAY_OVERVIEW, dayOverview);
        intent.putExtra(DayOverviewActivity.EXTRA_WEEK_OVERVIEW, weekOverview);
        startActivity(intent);
    }

    private boolean isLoggedIn() {
        return mLoggedIn;
    }
}
