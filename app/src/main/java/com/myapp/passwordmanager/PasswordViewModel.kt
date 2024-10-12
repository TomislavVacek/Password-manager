package com.myapp.passwordmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class PasswordViewModel : ViewModel() {

    var passwordList by mutableStateOf(listOf<PasswordItem>())
        private set

    fun addPassword(website: String, username: String, password: String) {
        val newPassword = PasswordItem(
            id = UUID.randomUUID().hashCode(), // Generira jedinstveni ID
            website = website,
            username = username,
            password = password
        )
        passwordList = passwordList + newPassword
    }

    fun deletePassword(passwordItem: PasswordItem) {
        passwordList = passwordList.filterNot { it.id == passwordItem.id }
    }
}