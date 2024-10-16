package com.myapp.passwordmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PasswordViewModelFactory(
    private val passwordDataStore: PasswordDataStore
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST") // Ovo ignorira upozorenje o unchecked cast
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PasswordViewModel::class.java)) {
            return PasswordViewModel(passwordDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

