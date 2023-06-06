package com.app.feather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.feather.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(APIManager(applicationContext)) {
            uploadTokens(
                AccountsManager(applicationContext)
                    .getTokens(SharedPreferencesManager(this@MainActivity))
            )
        }


//        findViewById<Button>(R.id.signInButton)?.setOnClickListener {
//            SharedPreferencesManager(this).saveRememberState(false)
//            closeSession()
//
//            startActivity(Intent(this, AuthorizationActivity::class.java))
//            finish()
//        }
    }

    override fun onDestroy() {
        closeSession()
        super.onDestroy()
    }

    private fun closeSession() {
        val userPreferencesManager = SharedPreferencesManager(this)
        if (!userPreferencesManager.isRemembered()) {
            userPreferencesManager.getUserLogin()?.let {
                AccountsManager(this).removeAccount(it)
            }
            userPreferencesManager.removeUserPreference()
        } //else userPreferencesManager.removeUserPreference()
    }
}