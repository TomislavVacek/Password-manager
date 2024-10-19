package com.myapp.passwordmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.Context
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class PasswordViewModel(private val passwordDataStore: PasswordDataStore) : ViewModel() {

    var passwordList by mutableStateOf(listOf<PasswordItem>())
        private set

    init {
        loadPasswords()  // Učitavanje spremljenih lozinki pri inicijalizaciji ViewModel-a
    }

    // Provjera ponovnog korištenja lozinke za različite web stranice
    fun isPasswordReused(password: String, website: String): Boolean {
        return passwordList.any { it.password == password && it.website != website }
    }

    // Dodaj novu lozinku
    fun addPassword(website: String, username: String, password: String): Boolean {
        val passwordReused = isPasswordReused(password, website)

        // Dodaj lozinku bez obzira na to je li ponovo korištena
        val newPassword = PasswordItem(
            id = (passwordList.maxOfOrNull { it.id } ?: 0) + 1,
            website = website,
            username = username,
            password = password
        )
        passwordList = passwordList + newPassword
        savePasswords()

        // Vraćamo true ako je lozinka već korištena
        return passwordReused
    }

    // Ažuriraj postojeću lozinku
    fun updatePassword(updatedItem: PasswordItem) {
        passwordList = passwordList.map { passwordItem ->
            if (passwordItem.id == updatedItem.id) {
                updatedItem  // Vraćamo ažuriranu lozinku
            } else {
                passwordItem  // Ako nije ta lozinka, vraćamo original
            }
        }
        savePasswords()  // Spremanje nakon uređivanja
    }

    // Izbriši lozinku
    fun deletePassword(passwordItem: PasswordItem) {
        passwordList = passwordList.filterNot { it.id == passwordItem.id }
        savePasswords()  // Spremanje nakon brisanja
    }

    // Učitaj lozinke iz DataStore-a
    private fun loadPasswords() {
        viewModelScope.launch {
            passwordList = passwordDataStore.getPasswords()
        }
    }

    // Spremi lozinke u DataStore
    private fun savePasswords() {
        viewModelScope.launch {
            passwordDataStore.savePasswords(passwordList)
        }
    }

    // Provjera snage lozinke
    fun checkPasswordStrength(password: String): String {
        var strengthScore = 0

        if (password.length >= 8) strengthScore++
        if (password.any { it.isDigit() }) strengthScore++
        if (password.any { it.isLowerCase() }) strengthScore++
        if (password.any { it.isUpperCase() }) strengthScore++
        if (password.any { !it.isLetterOrDigit() }) strengthScore++

        return when (strengthScore) {
            0, 1, 2 -> "Weak"
            3, 4 -> "Medium"
            5 -> "Strong"
            else -> "Weak"
        }
    }

    fun backupPasswords(context: Context) {
        val backupFile = File(context.getExternalFilesDir(null), "password_backup.txt")
        val fileWriter = FileWriter(backupFile)

        passwordList.forEach { passwordItem ->
            fileWriter.write("${passwordItem.website},${passwordItem.username},${passwordItem.password}\n")
        }
        fileWriter.close()
    }

    fun restorePasswords(context: Context) {
        val backupFile = File(context.getExternalFilesDir(null), "password_backup.txt")
        if (backupFile.exists()) {
            val fileReader = FileReader(backupFile)
            val bufferedReader = BufferedReader(fileReader)

            val restoredPasswords = mutableListOf<PasswordItem>()
            bufferedReader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 3) {
                    restoredPasswords.add(
                        PasswordItem(
                            id = (passwordList.maxOfOrNull { it.id } ?: 0) + 1,
                            website = parts[0],
                            username = parts[1],
                            password = parts[2]
                        )
                    )
                }
            }
            passwordList = restoredPasswords
            savePasswords()
            bufferedReader.close()
        }
    }

}

