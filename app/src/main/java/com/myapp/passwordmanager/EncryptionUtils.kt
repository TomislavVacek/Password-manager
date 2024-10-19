package com.myapp.passwordmanager

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

fun encryptPassword(password: String, secretKey: SecretKey): ByteArray {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(password.toByteArray())
}

fun decryptPassword(encryptedPassword: ByteArray, secretKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    return String(cipher.doFinal(encryptedPassword))
}
