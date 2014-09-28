package com.xebia.xtime.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.xebia.xtime.R;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;

public class Authenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "xtime";
    public static final String AUTH_TYPE = "cookie";
    private static final String TAG = "Authenticator";
    private final Context mContext;

    public Authenticator(final Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                             String authTokenType, String[] requiredFeatures,
                             Bundle options) throws NetworkErrorException {
        // tell Android to start the authentication activity
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.KEY_ADD_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                                     Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                               String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Get auth token: " + account + ", " + authTokenType);

        String password = AccountManager.get(mContext).getPassword(account);
        String cookie;
        try {
            Log.d(TAG, "Login request: " + account.name + ", " + password);
            cookie = XTimeWebService.getInstance().login(account.name, password);
            Log.d(TAG, "Login request result: " + cookie);
        } catch (IOException e) {
            Log.w(TAG, "Login request failed: '" + e.getMessage() + "'");
            throw new NetworkErrorException(e);
        }

        final Bundle result = new Bundle();
        if (TextUtils.isEmpty(cookie)) {
            Log.d(TAG, "Could not get auth token");
            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            result.putParcelable(AccountManager.KEY_INTENT, intent);
        } else {
            Log.d(TAG, "Return auth token: " + cookie);
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, cookie);
        }
        return result;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return mContext.getString(R.string.auth_token_label);
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                    String authTokenType,
                                    Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                              String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
