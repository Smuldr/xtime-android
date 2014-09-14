package com.xebia.xtime.shared;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import com.xebia.xtime.authenticator.Authenticator;

import java.io.IOException;

public final class CookieHelper {

    private CookieHelper() {
        // do not instantiate
    }

    public static String getCookie(final Context context) throws AuthenticatorException,
            OperationCanceledException, IOException {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
        return (null != accounts && accounts.length > 0)
                ? accountManager.blockingGetAuthToken(accounts[0], Authenticator.AUTH_TYPE, false)
                : null;
    }
}
