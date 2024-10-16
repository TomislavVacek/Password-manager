package com.myapp.passwordmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PasswordViewModel(private val passwordDataStore: PasswordDataStore) : ViewModel() {

    var passwordList by mutableStateOf(listOf<PasswordItem>())
        private set

    init {
        loadPasswords()  // Učitavanje spremljenih lozinki pri inicijalizaciji ViewModel-a
    }

    // Dodaj novu lozinku
    fun addPassword(website: String, username: String, password: String) {
        val newPassword = PasswordItem(
            id = (passwordList.maxOfOrNull { it.id } ?: 0) + 1,
            website = website,
            username = username,
            password = password
        )
        passwordList = passwordList + newPassword
        savePasswords()  // Spremanje nakon dodavanja
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

    // Izbriši lozinku
    fun deletePassword(passwordItem: PasswordItem) {
        passwordList = passwordList.filterNot { it.id == passwordItem.id }
        savePasswords()  // Spremanje nakon brisanja
    }
}