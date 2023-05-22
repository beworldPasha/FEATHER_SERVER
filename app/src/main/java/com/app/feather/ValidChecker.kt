package com.app.feather

class ValidChecker {
    fun checkEmailValid(email: String) =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun checkPassword(password: String) =
        ((password.length >= 7) and (password.length <= 20)) or (password.isEmpty())

    fun checkPassword(count: Int) = ((count >= 7) and (count <= 20)) or (count == 0)
}