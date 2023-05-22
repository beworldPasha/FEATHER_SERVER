package com.app.feather

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

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

    fun addAccount(login: String, password: String) {
        val account = Account(login, accountType)
        val keystoreManager = KeystoreManager()
        accountManager.addAccountExplicitly(
            account,
            keystoreManager.encryptPassword(password), null
        )
    }

    fun removeAccount(login: String, accountType: String) {
        val accounts = accountManager.getAccountsByType(accountType)
        for (account in accounts) {
            if (account.name == login) {
                accountManager.removeAccount(account, null, null)
                break
            }
        }
    }


    fun saveTokens() {
        val apiManager = APIManager(context)
        val jwtManager = JWTManager()

        val email = jwtManager.getEmail(apiManager.getAccessToken())
        val tokens = apiManager.getTokens()

        val accounts = accountManager.getAccountsByType(accountType)
        for (account in accounts) {
            if (account.name == email) {
                accountManager.setAuthToken(account, accessTag, tokens[0] as String)
                accountManager.setAuthToken(account, refreshTag, tokens[1] as String)
                break
            }
        }
    }

    fun getTokens() {

    }
}