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
    }

    private fun startSession() {
        val preferenceManager = SharedPreferencesManager(this)

        if (preferenceManager.isRemembered()) {
            val apiManager = APIManager(applicationContext)
            apiManager.uploadTokens(
                AccountsManager(applicationContext).getTokens(preferenceManager)
            )
            apiManager.refreshAndSaveTokens { isRefreshed ->
                if (isRefreshed) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } //TODO(): Сделать активити ошибки
            }
        } else {
            startActivity(Intent(this, AuthorizationActivity::class.java))
            finish()
        }
    }
}