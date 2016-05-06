package com.xebia.xtime.authenticator

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.xebia.xtime.R
import com.xebia.xtime.shared.fixStatusBarColor
import com.xebia.xtime.webservice.XTimeWebService
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.Cookie
import timber.log.Timber
import java.io.IOException

/**
 * Activity which displays a login screen to the user
 */
class AuthenticatorActivity : AppCompatActivity() {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var authTask: AuthenticationTask? = null
    private var username: String? = null
    private var password: String? = null

    // used to call back to the AccountManager
    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var resultBundle: Bundle? = null

    /**
     * Set the result that is to be sent as the result of the request that caused this Activity to be launched.
     * If result is null or this method is never called then the request will be canceled.
     *
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    private fun setAccountAuthenticatorResult(result: Bundle) {
        resultBundle = result
    }

    /**
     * Retrieves the AccountAuthenticatorResponse from either the intent or the icicle, if the icicle is non-zero.
     *
     * @param icicle the save instance data of this Activity, may be null
     */
    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        fixStatusBarColor()
        setContentView(R.layout.activity_login)

        if (icicle != null) {
            accountAuthenticatorResponse = icicle.getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
            accountAuthenticatorResponse?.onRequestContinued()
        } else {
            accountAuthenticatorResponse = intent
                    .getParcelableExtra<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
            accountAuthenticatorResponse?.onRequestContinued()
        }

        setSupportActionBar(toolbar)

        // Set up the login form.
        val username = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        usernameField.setText(username)

        val password = intent.getStringExtra(AccountManager.KEY_PASSWORD)
        passwordField.setText(password)
        passwordField.setOnEditorActionListener(TextView.OnEditorActionListener { textView, id, keyEvent ->
            if (id == R.id.login || id == EditorInfo.IME_NULL || id == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        submitButton.setOnClickListener { attemptLogin() }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        outState?.putParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse)
        super.onSaveInstanceState(outState, outPersistentState)
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        // send the result bundle back if set, otherwise send an error.
        if (resultBundle != null) {
            accountAuthenticatorResponse?.onResult(resultBundle)
        } else {
            accountAuthenticatorResponse?.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
        }
        accountAuthenticatorResponse = null
        super.finish()
    }

    /**
     * Attempts to sign in the account specified by the login form. If there are form errors
     * (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private fun attemptLogin() {
        if (authTask != null) {
            return
        }

        // Reset errors.
        usernameField.error = null
        passwordField.error = null

        // get the username and password
        val usernameValue = ("" + usernameField.text).trim { it <= ' ' }
        val passwordValue = ("" + passwordField.text).trim { it <= ' ' }

        var cancel = false
        var focusView: View? = null

        // Check for a valid password.
        if (TextUtils.isEmpty(passwordValue)) {
            passwordField.error = getString(R.string.error_field_required)
            focusView = passwordField
            cancel = true
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(usernameValue)) {
            usernameField.error = getString(R.string.error_field_required)
            focusView = usernameField
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to perform the login attempt
            login_status_message.setText(R.string.login_progress_signing_in)
            showProgress(true)
            authTask = AuthenticationTask()
            username = usernameValue.replace("@xebia.com", "").trim { it <= ' ' }
            password = passwordValue.trim { it <= ' ' }
            authTask!!.execute(username, password)
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        if (show) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(passwordField.windowToken, 0)
        }
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        loginLoadingIndicator.visibility = View.VISIBLE
        loginLoadingIndicator.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        loginLoadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })

        loginForm.visibility = View.VISIBLE
        loginForm.animate()
                .setDuration(shortAnimTime.toLong())
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        loginForm.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })
    }

    private fun onLoginSuccess(cookie: Cookie) {

        // add/update account in account manager
        val accountManager = AccountManager.get(this)
        val account = Account(username, Authenticator.ACCOUNT_TYPE)
        if (intent.getBooleanExtra(KEY_ADD_NEW_ACCOUNT, false)) {
            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            accountManager.addAccountExplicitly(account, password, null)
            accountManager.setAuthToken(account, Authenticator.AUTH_TYPE, cookie.value())
            accountManager.setPassword(account, password)
        } else {
            accountManager.setPassword(account, password)
        }

        // return authentication result
        val res = Intent()
        res.putExtra(AccountManager.KEY_ACCOUNT_NAME, username)
        res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE)
        res.putExtra(AccountManager.KEY_AUTHTOKEN, cookie.value())
        res.putExtra(AccountManager.KEY_PASSWORD, password)
        setAccountAuthenticatorResult(res.extras)
        setResult(RESULT_OK, res)
        finish()
    }

    /**
     * Represents an asynchronous login task used to authenticate the user. Expects Strings for
     * username and password as execution parameters.
     */
    private inner class AuthenticationTask : AsyncTask<String, Void, Cookie>() {

        private var exception: Exception? = null

        override fun doInBackground(vararg params: String): Cookie? {
            if (params.size < 2) {
                return null
            }
            val username = params[0]
            val password = params[1]
            val cookie: Cookie?
            try {
                Timber.d("Login request: %s", username)
                cookie = XTimeWebService.getInstance().login(username, password)
                Timber.d("Login request result: %s", cookie)
            } catch (e: IOException) {
                Timber.d(e, "Login request failed")
                exception = e
                cookie = null
            }

            return cookie
        }

        override fun onPostExecute(cookie: Cookie?) {
            authTask = null

            if (null != exception) {
                showProgress(false)
                Toast.makeText(this@AuthenticatorActivity, R.string.error_request_failed,
                        Toast.LENGTH_LONG).show()

            } else if (null == cookie) {
                showProgress(false)
                passwordField.error = getString(R.string.error_incorrect_password)
                passwordField.requestFocus()

            } else {
                onLoginSuccess(cookie)
            }
        }

        override fun onCancelled() {
            authTask = null
            showProgress(false)
        }
    }

    companion object {
        @JvmField
        val KEY_ADD_NEW_ACCOUNT = "com.xebia.xtime.extra.ADD_NEW_ACCOUNT"
    }
}
