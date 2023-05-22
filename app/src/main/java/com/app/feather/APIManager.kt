package com.app.feather

import android.content.Context
import android.util.Log
import com.feather.FeatherAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class APIManager(val context: Context?) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun signUp(login: String, password: String) {
        coroutineScope.launch {
            try {
                val answer = FeatherAPI.getInstance().authorize(
                    FeatherAPI.getInstance().SIGN_UP_REQUEST,
                    login,
                    password
                )
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
            }
        }
    }

    fun getTokens() = arrayOf(FeatherAPI.getInstance().tokens.split(' '))

    fun getAccessToken() = FeatherAPI.getInstance().tokens[0] as String

    fun signIn(login: String, password: String) {
        coroutineScope.launch {
            try {
                val answer = FeatherAPI.getInstance().authorize(
                    FeatherAPI.getInstance().SIGN_IN_REQUEST,
                    login,
                    password
                )
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
            }
        }
    }

    private fun refreshTokens() {
        coroutineScope.launch {
            try {
                FeatherAPI.getInstance().refreshTokens()
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
            }
        }
    }
}
