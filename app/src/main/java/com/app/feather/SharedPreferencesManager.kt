package com.app.feather

import android.app.Activity
import android.content.Context

class SharedPreferencesManager(activity: Activity?) {
    private val userKey = "current_user"
    private val rememberKey = "remember"
    private val userPreference = activity?.getSharedPreferences(
        activity.getString(R.string.userPreference), Context.MODE_PRIVATE)

    fun saveUserLogin(login: String?) {
        login?.also {
            with(userPreference?.edit() ?: return) {
                putString(userKey, it)
                apply()
            }
        } ?: return
    }

    fun saveRememberState(rememberState: Boolean) {
        with(userPreference?.edit() ?: return) {
            putBoolean(rememberKey, rememberState)
            apply()
        }
    }

    fun isRemembered(): Boolean {
        return with(userPreference ?: return false) {
            getBoolean(rememberKey, false)
        }
    }

    fun getUserLogin(): String? {
        return with(userPreference ?: return null) {
            getString(userKey, null)
        }
    }

    fun removeUserPreference() {
        userPreference?.edit().let {
            it?.remove(rememberKey)
            it?.remove(userKey)
        }
    }
}