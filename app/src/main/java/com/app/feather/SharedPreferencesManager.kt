package com.app.feather

import android.app.Activity
import android.content.Context

class SharedPreferencesManager(private val activity: Activity?) {
    private val userKey = "current_user"

    fun saveUserLogin(login: String?) {
        val userPreference = activity?.getSharedPreferences(
            activity.getString(R.string.userPreference), Context.MODE_PRIVATE) ?: return

        login?.also {
            with(userPreference.edit()) {
                putString(userKey, it)
                apply()
            }
        } ?: return
    }
}