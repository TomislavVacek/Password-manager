package com.myapp.passwordmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PasswordManagerApp()
        }
    }
}

@Composable
fun PasswordManagerApp(viewModel: PasswordViewModel = viewModel()) {
    var website by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordLength by remember { mutableFloatStateOf(12f) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Password Manager") })
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

            Spacer(modifier = Modifier.height(16.dp))

            // Dodaj Slider za odabir duljine lozinke
            Text(text = "Password Length: ${passwordLength.toInt()}") // Prikazuje trenutnu duljinu
            Slider(
                value = passwordLength,
                onValueChange = { passwordLength = it },
                valueRange = 8f..20f, // Raspon duljine lozinke
                steps = 12, // Dodaj korake (maksimalni broj koraka između 8 i 20)
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dodaj gumb za generiranje lozinke
            Button(onClick = {
                password = PasswordGenerator.generatePassword(passwordLength.toInt()) // Generiraj lozinku na temelju duljine
            }) {
                Text("Generate Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.addPassword(website, username, password)
                website = ""
                username = ""
                password = ""
            }) {
                Text("Add Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            viewModel.passwordList.forEach { passwordItem ->
                Text("Website: ${passwordItem.website}, Username: ${passwordItem.username}, Password: ${passwordItem.password}")
            }
        }
    }
}