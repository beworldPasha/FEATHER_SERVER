package com.app.feather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = JWTManager()
        manager.getEmail("")
        //startActivity(Intent(this, AuthorizationActivity::class.java))
    }

    fun checkingForAuth() {

    }
}