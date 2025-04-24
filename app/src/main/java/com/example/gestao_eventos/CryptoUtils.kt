package com.example.gestao_eventos

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    // 32 bytes = 256 bits
    private const val SECRET_KEY = "12345678901234567890123456789012"
    // 16 bytes = 128 bits
    private const val IV        = "abcdefghijklmnop"

    private fun getKeySpec() = SecretKeySpec(
        SECRET_KEY.toByteArray(Charsets.UTF_8),
        "AES"
    )
    private fun getIvSpec() = IvParameterSpec(
        IV.toByteArray(Charsets.UTF_8)
    )

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), getIvSpec())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(cipherText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), getIvSpec())
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            String(cipher.doFinal(decoded), Charsets.UTF_8)
        } catch (e: Exception) {
            ""  // ou lança, conforme queiras tratar o erro
        }
    }
}
