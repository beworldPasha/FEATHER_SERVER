package com.app.feather

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


    }

    override fun onResume() {
        super.onResume()
        startSession()
        //startActivity(Intent(this, AuthorizationActivity::class.java))
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun startSession() {
        val preferenceManager = SharedPreferencesManager(this)
        if (preferenceManager.isRemembered() == true) {
            val accessToken = AccountsManager(applicationContext).
            getAccessToken(preferenceManager.getUserLogin())

            if (accessToken?.let { JWTManager().isAccessExpired(it) } == true) {
                APIManager(applicationContext).refreshTokens()
            }
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, AuthorizationActivity::class.java))
        }
    }
}