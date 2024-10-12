package com.myapp.passwordmanager

data class PasswordItem(
    val id: Int = 0, // Jedinstveni ID za svaku lozinku
    val website: String = "",
    val username: String = "",
    val password: String = ""
)