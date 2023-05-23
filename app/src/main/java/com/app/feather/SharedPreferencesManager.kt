package com.app.feather

import android.app.Activity
import android.content.Context

class SharedPreferencesManager(private val activity: Activity?) {
    private val userKey = "current_user"
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

    fun getUserLogin(): String? {
        return with(userPreference ?: return null) {
            getString(userKey, null)
        }
    }
}