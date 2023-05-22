package com.app.feather

import android.accounts.AccountManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class AuthenticateService : Service() {
    private lateinit var authenticator: Authenticator

    override fun onCreate() {
        authenticator = Authenticator(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return if (AccountManager.ACTION_AUTHENTICATOR_INTENT == intent?.action)
            authenticator.iBinder
        else null
    }
}