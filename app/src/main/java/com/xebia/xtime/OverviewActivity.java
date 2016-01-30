package com.xebia.xtime;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;

import com.xebia.xtime.authenticator.Authenticator;
import com.xebia.xtime.dayoverview.DayOverviewActivity;
import com.xebia.xtime.monthoverview.MonthPagerFragment;
import com.xebia.xtime.shared.model.DayOverview;
import com.xebia.xtime.weekoverview.DailyHoursListFragment;
import com.xebia.xtime.weekoverview.WeekPagerFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class OverviewActivity extends AppCompatActivity implements DailyHoursListFragment.Listener {

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountManager = AccountManager.get(this);
        if (hasXTimeAccount()) {
            setListNavigation();
        } else {
            Timber.d("Add account");
            AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    try {
                        Timber.d("AccountManagerCallback called");
                        if (future.isDone()) {
                            Bundle result = future.getResult();
                            String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                            Timber.i("Auth token: '%s'", authToken);
                            setListNavigation();
                        }
                    } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                        Timber.e(e, "Failed to get auth token");
                    }
                }
            };
            mAccountManager.addAccount(Authenticator.ACCOUNT_TYPE, Authenticator.AUTH_TYPE, null,
                    null, this, callback, null);
        }
    }

    private boolean hasXTimeAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        return accounts.length > 0;
    }

    private void setListNavigation() {

        final List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("title", getString(R.string.title_week_overview));
        map.put("fragment", Fragment.instantiate(this, WeekPagerFragment.class.getName()));
        data.add(map);
        map = new HashMap<>();
        map.put("title", getString(R.string.title_month_overview));
        map.put("fragment", Fragment.instantiate(this, MonthPagerFragment.class.getName()));
        data.add(map);
        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.ab_nav_dropdown, new String[]{"title"},
                new int[]{android.R.id.text1});

        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setListNavigationCallbacks(adapter,
                    new ActionBar.OnNavigationListener() {
                        @Override
                        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                            Map<String, Object> map = data.get(itemPosition);
                            Object o = map.get("fragment");
                            if (o instanceof Fragment) {
                                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                                tx.replace(android.R.id.content, (Fragment) o);
                                tx.commit();
                            }
                            return true;
                        }
                    }
            );
        }

        map = data.get(0);
        Object o = map.get("fragment");
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(android.R.id.content, (Fragment) o);
        tx.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            // TODO: logout
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(DayOverview overview) {
        Intent intent = new Intent(this, DayOverviewActivity.class);
        intent.putExtra(DayOverviewActivity.EXTRA_DAY_OVERVIEW, overview);
        startActivity(intent);
    }
}
