package com.xebia.xtime.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xebia.xtime.R;
import com.xebia.xtime.webservice.XTimeWebService;

import java.io.IOException;

/**
 * Activity which displays a login screen to the user
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public static final String KEY_ADD_NEW_ACCOUNT = "com.xebia.xtime.extra.ADD_NEW_ACCOUNT";
    private static final String TAG = "AuthenticatorActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private AuthenticationTask mAuthTask = null;
    private String mUsername;
    private String mPassword;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        String username = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        mUsernameView.setText(username);

        mPasswordView = (EditText) findViewById(R.id.password);
        String password = getIntent().getStringExtra(AccountManager.KEY_PASSWORD);
        mPasswordView.setText(password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL ||
                        id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // get the username and password
        String username = ("" + mUsernameView.getText()).trim();
        String password = ("" + mPasswordView.getText()).trim();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the login attempt
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new AuthenticationTask();
            mUsername = username.trim();
            mPassword = password.trim();
            mAuthTask.execute(mUsername, mPassword);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginStatusView.setVisibility(View.VISIBLE);
        mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });

        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
    }

    private void finishLogin(String cookie) {

        // add/update account in account manager
        AccountManager accountManager = AccountManager.get(this);
        final Account account = new Account(mUsername, Authenticator.ACCOUNT_TYPE);
        if (getIntent().getBooleanExtra(KEY_ADD_NEW_ACCOUNT, false)) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate
            // the user)
            accountManager.addAccountExplicitly(account, mPassword, null);
            accountManager.setAuthToken(account, Authenticator.AUTH_TYPE, cookie);
            accountManager.setPassword(account, mPassword);
        } else {
            accountManager.setPassword(account, mPassword);
        }

        // return authentication result
        final Intent res = new Intent();
        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
        res.putExtra(AccountManager.KEY_AUTHTOKEN, cookie);
        res.putExtra(AccountManager.KEY_PASSWORD, mPassword);
        setAccountAuthenticatorResult(res.getExtras());
        setResult(RESULT_OK, res);
        finish();
    }

    /**
     * Represents an asynchronous login task used to authenticate the user. Expects Strings for
     * username and password as execution parameters.
     */
    private class AuthenticationTask extends AsyncTask<String, Void, String> {

        private Exception mException;

        @Override
        protected String doInBackground(String... params) {
            if (null == params || params.length < 2) {
                return null;
            }
            String username = params[0];
            String password = params[1];
            String cookie;
            try {
                Log.d(TAG, "Login request: " + username + ", " + password);
                cookie = XTimeWebService.getInstance().login(username, password);
                Log.d(TAG, "Login request result: " + cookie);
            } catch (IOException e) {
                Log.w(TAG, "Login request failed: '" + e.getMessage() + "'");
                mException = e;
                cookie = null;
            }
            return cookie;
        }

        @Override
        protected void onPostExecute(final String cookie) {
            mAuthTask = null;
            showProgress(false);

            if (null != mException) {
                Toast.makeText(AuthenticatorActivity.this, R.string.error_request_failed,
                        Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(cookie)) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();

            } else {
                finishLogin(cookie);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
