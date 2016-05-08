package com.xebia.xtime.authenticator

import android.accounts.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.xebia.xtime.BuildConfig
import com.xebia.xtime.R
import com.xebia.xtime.webservice.XTimeWebService
import okhttp3.Cookie
import timber.log.Timber
import java.io.IOException

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle? {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String,
                            authTokenType: String?, requiredFeatures: Array<String>?,
                            options: Bundle?): Bundle? {
        // tell Android to start the authentication activity
        val intent = Intent(context, AuthenticatorActivity::class.java)
        intent.putExtra(AuthenticatorActivity.KEY_ADD_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle?): Bundle? {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle?): Bundle? {
        Timber.d("Get auth token for account %s, type: %s", account, authTokenType)
        val cookie: Cookie? = loginWithPassword(account)
        return createAuthResultBundle(response, account, cookie)
    }

    override fun getAuthTokenLabel(authTokenType: String): String? {
        return context.getString(R.string.auth_token_label)
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account, authTokenType: String?, options: Bundle?): Bundle? {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle? {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    private fun loginWithPassword(account: Account): Cookie? {
        Timber.d("Login request: %s", account.name)
        try {
            val password: String? = AccountManager.get(context).getPassword(account)
            return XTimeWebService.getInstance().login(account.name, password)
        } catch (e: IOException) {
            Timber.e(e, "Login request failed")
            throw NetworkErrorException(e)
        }
    }

    private fun createAuthResultBundle(response: AccountAuthenticatorResponse, account: Account, cookie: Cookie?): Bundle {
        val result = Bundle()
        if (null == cookie) {
            Timber.w("Could not get auth token")
            val intent = Intent(context, AuthenticatorActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            result.putParcelable(AccountManager.KEY_INTENT, intent)
        } else {
            Timber.d("Return auth token: %s", cookie.value())
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, cookie.value())
        }
        return result
    }

    companion object {
        @JvmField
        val ACCOUNT_TYPE = if (BuildConfig.DEBUG) "xtime-debug" else "xtime"
        @JvmField
        val AUTH_TYPE = "cookie"
    }
}
