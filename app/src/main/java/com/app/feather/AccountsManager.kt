package com.app.feather

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountsManager(private val context: Context?) {
    private val accountManager = AccountManager.get(context)
    private val accountType = context?.getString(R.string.accountType)

    private val accessTag = "access_token"
    private val refreshTag = "refresh_token"

    fun getAccounts(): Array<Account> = accountManager.getAccountsByType(accountType)

    fun getPassword(account: Account): String? {
        val keystoreManager = KeystoreManager()
        val encryptedPassword = accountManager.getPassword(account)

        return keystoreManager.decryptPassword(encryptedPassword)
    }

    fun addAccount(email: String, password: String?) {
        val keystoreManager = KeystoreManager()

        if (isAccountExist(email)) {
            password?.let {
                accountManager.setPassword(
                    getAccount(email),
                    keystoreManager.encryptPassword(password)
                )
            }
            return
        }
        val account = Account(email, accountType)
        password?.let {
            accountManager.addAccountExplicitly(
                account, keystoreManager.encryptPassword(password), null
            )
            return
        }
        accountManager.addAccountExplicitly(account, null, null)
    }

    private fun isAccountExist(email: String) = getAccount(email) != null

    fun removeAccount(email: String) {
        getAccount(email)?.let { accountManager.removeAccount(it, null, null) }
    }

    fun getAccessToken(email: String?, callback: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            email?.let {
                val account = getAccount(email)
                val bundle = accountManager?.getAuthToken(
                    account, accessTag, false, null, null
                )?.result
                withContext(Dispatchers.Main) {
                    callback(bundle?.getString(accessTag, null))
                }
            }
            withContext(Dispatchers.IO) {
                callback(null)
            }
        }
    }

    //TODO() Сохранение токенов в Main при повторном входе срабатывает до их обновления в
    // SplashScreen, если вход Remember Me
    fun saveTokens(email: String?) {
        val apiManager = APIManager(context)
        apiManager.getTokens()?.let { tokens ->
            val account = getAccount(email ?: return)
            if (account == null) {
                addAccount(email, null)
            }
            with(getAccount(email)) {
                accountManager.setAuthToken(this, accessTag, tokens[0])
                accountManager.setAuthToken(this, refreshTag, tokens[1])
            }
        }
    }

    fun isTokensSaved(userPreferencesManager: SharedPreferencesManager) =
        getTokens(userPreferencesManager) == null

    fun removeTokens() {

    }

    private fun getAccount(email: String): Account? {
        val accounts = accountManager.getAccountsByType(accountType)
        for (account in accounts) {
            if (account.name == email) return account
        }
        return null
    }

    fun getTokens(userPreferencesManager: SharedPreferencesManager): List<String>? {
        userPreferencesManager.getUserLogin()?.let { email ->
            getAccount(email)?.let {
                val accessToken = accountManager.peekAuthToken(it, accessTag)
                val refreshToken = accountManager.peekAuthToken(it, refreshTag)

                accessToken?.let { access ->
                    refreshToken?.let { refresh ->
                        return listOf(access, refresh)
                    }
                }
            }
        }
        return null
    }
}