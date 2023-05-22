package com.app.feather

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class KeystoreManager {
    private val keyAlias = "alias_Password"
    private val provider = "AndroidKeyStore"
    private val transformation = "AES/CBC/PKCS7Padding"
    private val keyStore: KeyStore = KeyStore.getInstance(provider)

    init {
        keyStore.load(null)
    }

    fun encryptPassword(password: String): String? {
        val secretKey = getOrCreateSecretKey() ?: return null
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(password.toByteArray(StandardCharsets.UTF_8))

        val encryptedData = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, encryptedData, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, encryptedData, iv.size, encryptedBytes.size)

        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    fun deleteSecretKey() {
        keyStore.deleteEntry(keyAlias)
    }

    fun decryptPassword(password: String): String? {
        val secretKey = getSecretKey() ?: return null
        val encryptedPassword = Base64.decode(password, Base64.DEFAULT)

        val iv = encryptedPassword.sliceArray(0 until 16)
        val encryptedBytes = encryptedPassword
            .sliceArray(16 until encryptedPassword.size)

        val cipher = Cipher.getInstance(transformation)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey? {
        val keyEntry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry

        return if (keyEntry != null) {
            keyEntry.secretKey
        } else createSecretKey()
    }

    private fun getSecretKey(): SecretKey? {
        val keyEntry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        return keyEntry?.secretKey
    }

    private fun createSecretKey(): SecretKey? {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, provider)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(keyGenSpec)
        return keyGenerator.generateKey()
    }
}
