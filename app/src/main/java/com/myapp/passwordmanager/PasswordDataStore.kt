package com.myapp.passwordmanager

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken




class PasswordDataStore(context: Context) {

    // Ključ za enkripciju
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Inicijalizacija EncryptedSharedPreferences
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "passwords_datastore",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Gson za serijalizaciju/deserijalizaciju liste lozinki
    private val gson = Gson()
    private val passwordListKey = "password_list"

    // Funkcija za spremanje lozinki
    fun savePasswords(passwordList: List<PasswordItem>) {
        val json = gson.toJson(passwordList)
        sharedPreferences.edit().putString(passwordListKey, json).apply()
    }

    // Funkcija za dohvaćanje spremljenih lozinki
    fun getPasswords(): List<PasswordItem> {
        val json = sharedPreferences.getString(passwordListKey, null) ?: return emptyList()
        val type = object : TypeToken<List<PasswordItem>>() {}.type
        return gson.fromJson(json, type)
    }
}