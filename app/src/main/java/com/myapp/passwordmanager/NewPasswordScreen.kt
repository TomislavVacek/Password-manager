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
    val currentPasswordLength by remember { mutableFloatStateOf(passwordLength) }

    // Stanje za prikaz snage lozinke
    val passwordStrength = viewModel.checkPasswordStrength(currentPassword)

    // Stanje za prikaz Snackbar-a
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // LaunchedEffect za prikazivanje Snackbar-a
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            scaffoldState.snackbarHostState.showSnackbar(snackbarMessage)
            showSnackbar = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(action = {
                    TextButton(onClick = { data.dismiss() }) {
                        Text("OK")
                    }
                }) {
                    Text(snackbarMessage)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // Prikaz snage lozinke
            Text(text = "Password Strength: $passwordStrength")

            // Ostali elementi, gumbi za dodavanje lozinke, generiranje lozinke itd.
            Button(onClick = {
                currentPassword = PasswordGenerator.generatePassword(currentPasswordLength.toInt())
            }) {
                Text("Generate Password")
            }

            Button(onClick = {
                coroutineScope.launch {
                    val isPwned = viewModel.isPasswordPwned(currentPassword)
                    snackbarMessage = if (isPwned) {
                        "Warning: This password has been compromised!"
                    } else {
                        "This password is safe."
                    }
                    showSnackbar = true
                }
            }) {
                Text("Check if Password is Pwned")
            }

            // Gumb za dodavanje lozinke
            Button(
                onClick = {
                    if (viewModel.isPasswordReused(currentPassword, currentWebsite)) {
                        snackbarMessage = "This password has already been used for another website!"
                        showSnackbar = true
                    }
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
                    onPasswordAdded()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Password" else "Add Password")
            }
        }
    }
}



