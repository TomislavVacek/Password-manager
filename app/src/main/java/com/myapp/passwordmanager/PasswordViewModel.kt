package com.myapp.passwordmanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PasswordViewModel(private val passwordDataStore: PasswordDataStore) : ViewModel() {

    // Lista lozinki koja se drži u stanju Compose-a
    var passwordList by mutableStateOf(listOf<PasswordItem>())
        private set

    init {
        loadPasswords()  // Učitavanje spremljenih lozinki pri inicijalizaciji ViewModel-a
    }

    // Dodaj novu lozinku
    fun addPassword(website: String, username: String, password: String) {
        val newPassword = PasswordItem(
            id = (passwordList.maxOfOrNull { it.id } ?: 0) + 1,  // Pronađi najveći postojeći ID i dodaj 1
            website = website,
            username = username,
            password = password
        )
        passwordList = passwordList + newPassword
        savePasswords()  // Spremanje nakon dodavanja nove lozinke
    }

    // Ažuriraj postojeću lozinku
    fun updatePassword(updatedItem: PasswordItem) {
        passwordList = passwordList.map { passwordItem ->
            if (passwordItem.id == updatedItem.id) {
                updatedItem  // Vraćamo ažuriranu lozinku
            } else {
                passwordItem  // Ako nije ta lozinka, vraćamo originalnu lozinku
            }
        }
        savePasswords()  // Spremanje nakon uređivanja lozinke
    }

    // Izbriši lozinku
    fun deletePassword(passwordItem: PasswordItem) {
        passwordList = passwordList.filterNot { it.id == passwordItem.id }  // Filtriraj iz liste lozinki
        savePasswords()  // Spremanje nakon brisanja
    }

    // Učitaj lozinke iz DataStore-a
    private fun loadPasswords() {
        viewModelScope.launch {
            passwordList = passwordDataStore.getPasswords()  // Učitavanje spremljenih lozinki iz DataStore-a
        }
    }

    // Spremi lozinke u DataStore
    private fun savePasswords() {
        viewModelScope.launch {
            passwordDataStore.savePasswords(passwordList)  // Spremanje liste lozinki u DataStore
        }
    }
}