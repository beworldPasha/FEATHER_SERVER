package com.app.feather

import android.content.Context
import android.util.Log
import com.feather.FeatherAPI
import com.feather.Playlist
import com.feather.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class APIManager(val context: Context?) {

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun signUp(login: String, password: String, callback: (Boolean) -> Unit) {
        coroutineScope.launch {
            val result: Boolean = try {
                FeatherAPI.getInstance().authorize(
                    FeatherAPI.getInstance().SIGN_UP_REQUEST,
                    login,
                    password
                )
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
                false
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    fun uploadTokens(tokens: List<String>?) {
        tokens?.let {
            FeatherAPI.getInstance().fetchTokens("${tokens[0]} ${tokens[1]}")
        }

    }

    fun getTokens() = FeatherAPI.getInstance().tokens?.split(' ')

    fun getAccessToken() = FeatherAPI.getInstance().tokens[0] as String

    fun signIn(login: String, password: String, callback: (Boolean) -> Unit) {
        coroutineScope.launch {
            var result: Boolean
            try {
                result = FeatherAPI.getInstance().authorize(
                    FeatherAPI.getInstance().SIGN_IN_REQUEST,
                    login,
                    password
                )

            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
                result = false
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    fun refreshAndSaveTokens(callback: (Boolean) -> Unit) {
        coroutineScope.launch {
            val result = try {
                FeatherAPI.getInstance().refreshTokens()
                AccountsManager(context).saveTokens(
                    JWTManager().getEmail(FeatherAPI.getInstance().accessToken)
                )
                true
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
                false
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    fun getPlaylist(
        artist: String = "Amaranthe",
        playlist: String = "Amaranthe",
        callback: (Playlist?) -> Unit
    ) {
        coroutineScope.launch {
            val result: Playlist? = try {
                FeatherAPI.getInstance().fetchData("$artist/$playlist", Playlist::class.java)
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
                null
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

    fun getSong(
        artist: String,
        playlist: String,
        song: String,
        callback: (Song?) -> Unit
    ) {
        coroutineScope.launch {
            val result: Song? = try {
                FeatherAPI.getInstance().fetchData("$artist/$playlist/$song", Song::class.java)
            } catch (exception: Exception) {
                Log.e("API MANAGER", exception.message ?: "")
                null
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }
}
