package com.myapp.passwordmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun NewPasswordScreen(
    viewModel: PasswordViewModel,
    website: String,
    username: String,
    password: String,
    passwordLength: Float,
    isEditing: Boolean,
    editingPasswordItem: PasswordItem?,
    onPasswordAdded: () -> Unit
) {
    var currentWebsite by remember { mutableStateOf(website) }
    var currentUsername by remember { mutableStateOf(username) }
    var currentPassword by remember { mutableStateOf(password) }
    var currentPasswordLength by remember { mutableFloatStateOf(passwordLength) }

    // Stanje za prikaz Snackbar-a
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    action = {
                        TextButton(onClick = { data.dismiss() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(data.message)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp) // Smanjen razmak između elemenata
        ) {
            // Polja za unos
            OutlinedTextField(
                value = currentWebsite,
                onValueChange = { currentWebsite = it },
                label = { Text("Website") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentUsername,
                onValueChange = { currentUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            // Provjera snage lozinke
            val passwordStrength = viewModel.checkPasswordStrength(currentPassword)
            Text(text = "Password Strength: $passwordStrength")

            Text(text = "Password Length: ${currentPasswordLength.toInt()}")
            Slider(
                value = currentPasswordLength,
                onValueChange = { currentPasswordLength = it },
                valueRange = 8f..20f,
                steps = 12,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Gumb za generiranje lozinke
            Button(
                onClick = {
                    currentPassword = PasswordGenerator.generatePassword(currentPasswordLength.toInt())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Password")
            }

            // Gumb za provjeru lozinke na Have I Been Pwned API
            Button(
                onClick = {
                    coroutineScope.launch {
                        val isPwned = viewModel.isPasswordPwned(currentPassword)
                        val snackbarMessage = if (isPwned) {
                            "Warning: This password has been compromised!"
                        } else {
                            "This password is safe."
                        }
                        scaffoldState.snackbarHostState.showSnackbar(snackbarMessage)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check if Password is Pwned")
            }

            // Gumb za dodavanje lozinke
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Provjera korištene lozinke
                        val isReused = viewModel.isPasswordReused(currentPassword, currentWebsite)
                        if (isReused) {
                            scaffoldState.snackbarHostState.showSnackbar("This password has already been used for another website!")
                        }

                        // Dodaj ili ažuriraj lozinku nakon upozorenja
                        if (isEditing && editingPasswordItem != null) {
                            viewModel.updatePassword(
                                PasswordItem(
                                    id = editingPasswordItem.id,
                                    website = currentWebsite,
                                    username = currentUsername,
                                    password = currentPassword
                                )
                            )
                        } else {
                            viewModel.addPassword(currentWebsite, currentUsername, currentPassword)
                        }

                        // Prikaži poruku o uspješnom dodavanju samo jednom
                        if (!isReused) {
                            scaffoldState.snackbarHostState.showSnackbar("Password successfully added!")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Password" else "Add Password")
            }
        }
    }
}

