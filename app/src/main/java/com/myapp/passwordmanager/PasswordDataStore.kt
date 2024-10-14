package com.myapp.passwordmanager

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PasswordDataStore(context: Context) {
    private val masterKey = MasterKey.Builder(context, "master_key")
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context, // Prvo ide Context
        "password_preferences", // Ime datoteke
        masterKey, // MasterKey objekt
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePasswords(passwords: List<PasswordItem>) {
        val jsonString = Json.encodeToString(passwords)
        sharedPrefs.edit().putString("passwords", jsonString).apply()
    }

    fun getPasswords(): List<PasswordItem> {
        val jsonString = sharedPrefs.getString("passwords", null) ?: ""
        return if (jsonString.isNotEmpty()) {
            Json.decodeFromString(jsonString)
        } else {
            emptyList()
        }
    }
}