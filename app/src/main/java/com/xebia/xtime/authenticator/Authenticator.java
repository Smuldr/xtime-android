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

import com.xebia.xtime.R;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;

import timber.log.Timber;

public class Authenticator extends AbstractAccountAuthenticator {

    public static final String ACCOUNT_TYPE = "xtime";
    public static final String AUTH_TYPE = "cookie";
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
        Timber.w("Get auth token for account %s, type: %s", account, authTokenType);

        String password = AccountManager.get(mContext).getPassword(account);
        String cookie;
        try {
            Timber.w("Login request: %s, %s", account.name, password);
            cookie = XTimeWebService.getInstance().login(account.name, password);
            Timber.w("Login request result: %s", cookie);
        } catch (IOException e) {
            Timber.e(e,"Login request failed");
            throw new NetworkErrorException(e);
        }

        final Bundle result = new Bundle();
        if (TextUtils.isEmpty(cookie)) {
            Timber.d("Could not get auth token");
            final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            result.putParcelable(AccountManager.KEY_INTENT, intent);
        } else {
            Timber.d("Return auth token: %s", cookie);
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
