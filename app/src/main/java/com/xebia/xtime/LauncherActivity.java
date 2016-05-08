package com.xebia.xtime;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.xebia.xtime.authenticator.Authenticator;

import java.io.IOException;

import timber.log.Timber;

/**
 * Launcher activity
 *
 * Checks if an XTime account has been set up on the phone and starts the
 * {@link OverviewActivity} if it has.  If there is not XTime account present, the
 * {@link android.accounts.AccountManager} is invoked to add one.
 */
public class LauncherActivity extends Activity {

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountManager = AccountManager.get(this);
        if (hasXTimeAccount()) {
            startOverviewActivity();
        } else {
            addAccount();
        }
    }

    private boolean hasXTimeAccount() {
        Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        return accounts.length > 0;
    }

    private void addAccount() {
        Timber.d("Add account");
        accountManager.addAccount(Authenticator.ACCOUNT_TYPE, Authenticator.AUTH_TYPE, null,
                null, this, new XTimeAccountCallback(this), null);
    }

    private void startOverviewActivity() {
        Timber.d("Start OverviewActivity");
        startActivity(new Intent(this, OverviewActivity.class));
        finish();
    }

    private static class XTimeAccountCallback implements AccountManagerCallback<Bundle> {

        private final LauncherActivity activity;

        XTimeAccountCallback(final LauncherActivity activity) {
            this.activity = activity;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> future) {
            Timber.d("AccountManagerCallback called");
            if (future.isCancelled()) {
                Timber.d("Account setup cancelled");
                activity.finish();
            } else if (future.isDone()) {
                try {
                    future.getResult();
                    activity.startOverviewActivity();
                } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                    Timber.e(e, "Failed to get authenticator result");
                    activity.finish();
                }
            } else {
                Timber.w("Something is very wrong with the AccountManager");
                activity.finish();
            }
        }
    }
}
