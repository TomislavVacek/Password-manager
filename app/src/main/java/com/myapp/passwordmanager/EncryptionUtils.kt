package com.myapp.passwordmanager

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {

    private const val AES = "AES"
    private const val AES_MODE = "AES/CBC/PKCS5Padding"
    private const val CHARSET = "UTF-8"

    // Generiranje tajnog ključa za AES enkripciju
    fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(AES)
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    // Enkripcija teksta
    fun encrypt(plainText: String, secretKey: SecretKey): Pair<String, String> {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv // Inicijalizacijski vektor (IV) koji je potreban za dekripciju
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(charset(CHARSET)))

        val encryptedText = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        return Pair(encryptedText, ivString)
    }

    // Dekripcija šifriranog teksta
    fun decrypt(encryptedText: String, ivString: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val iv = Base64.decode(ivString, Base64.DEFAULT)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

        val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes, charset(CHARSET))
    }

    // Pretvaranje String ključa u SecretKey objekt
    fun getSecretKeyFromString(key: String): SecretKey {
        val decodedKey = Base64.decode(key, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, 0, decodedKey.size, AES)
    }

    // Pretvaranje SecretKey objekta u String za pohranu ili dijeljenje
    fun secretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }
}
