package com.myapp.passwordmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val scaffoldState = rememberScaffoldState() // Dodaj scaffoldState za kontrolu Snackbar-a

    // LaunchedEffect za prikazivanje Snackbar-a
    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            scaffoldState.snackbarHostState.showSnackbar(snackbarMessage)
            showSnackbar = false // Resetiranje stanja kako bi se izbjeglo ponavljanje
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,  // Dodaj scaffoldState u Scaffold
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
            verticalArrangement = Arrangement.Top
        ) {

            OutlinedTextField(
                value = currentWebsite,
                onValueChange = { currentWebsite = it },
                label = { Text("Website") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currentUsername,
                onValueChange = { currentUsername = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            // Provjera snage lozinke
            val passwordStrength = viewModel.checkPasswordStrength(currentPassword)
            Text(text = "Password Strength: $passwordStrength")

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Password Length: ${currentPasswordLength.toInt()}")
            Slider(
                value = currentPasswordLength,
                onValueChange = { currentPasswordLength = it },
                valueRange = 8f..20f,
                steps = 12,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                currentPassword = PasswordGenerator.generatePassword(currentPasswordLength.toInt())
            }) {
                Text("Generate Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Provjeravamo postoji li već lozinka na drugoj web stranici
                if (viewModel.isPasswordReused(currentPassword, currentWebsite)) {
                    snackbarMessage = "This password has already been used for another website!"
                    showSnackbar = true  // Postavi poruku i prikaži Snackbar
                }

                // Dozvoljavanje unosa čak i ako je lozinka već korištena
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

                onPasswordAdded() // Resetiraj formu nakon dodavanja ili uređivanja
            }) {
                Text(if (isEditing) "Update Password" else "Add Password")
            }
        }
    }
}
