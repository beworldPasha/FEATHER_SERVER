package com.app.feather

import com.auth0.android.jwt.JWT

class JWTManager {
    fun isAccessExpired(accessToken: String): Boolean {
        val accessToken = JWT(accessToken)
        return accessToken.isExpired(30)
    }

    fun getEmail(accessToken: String) = JWT(accessToken).getClaim("usr").asString()!!
}