package com.myapp.passwordmanager

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun NewPasswordScreen(
    viewModel: PasswordViewModel,
    onPasswordAdded: () -> Unit // Callback nakon što je lozinka dodana
) {
    var website by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordLength by remember { mutableFloatStateOf(12f) }

    // Držimo stanje za prikazivanje Snackbar-a
    var showSnackbar by remember { mutableStateOf(false) }

    // Provjera snage lozinke
    val passwordStrength = viewModel.checkPasswordStrength(password)

    // Provjera ponovnog korištenja lozinke
    val isPasswordReused = viewModel.isPasswordReused(password)

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        OutlinedTextField(
            value = website,
            onValueChange = { website = it },
            label = { Text("Website") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        // Prikaz snage lozinke
        Text(text = "Password Strength: $passwordStrength")

        // Prikaz upozorenja ako je lozinka već korištena
        if (isPasswordReused) {
            Text(text = "Warning: This password has already been used!", color = MaterialTheme.colors.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Password Length: ${passwordLength.toInt()}")
        Slider(
            value = passwordLength,
            onValueChange = { passwordLength = it },
            valueRange = 8f..20f,
            steps = 12,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            password = PasswordGenerator.generatePassword(passwordLength.toInt())
        }) {
            Text("Generate Password")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (!isPasswordReused) {
                viewModel.addPassword(website, username, password)
                website = ""
                username = ""
                password = ""
                onPasswordAdded()
            } else {
                showSnackbar = true // Pokaži snackbar ako se lozinka ponavlja
            }
        }) {
            Text("Add Password")
        }

        // Prikazivanje Snackbar-a za ponovnu upotrebu lozinke
        if (showSnackbar) {
            Snackbar(
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("This password is already used!")
            }
        }
    }
}

