package com.myapp.passwordmanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.fragment.app.FragmentActivity


class MainActivity : FragmentActivity() {  // Promijenili smo ComponentActivity u FragmentActivity
    private lateinit var biometricManager: BiometricManager
    private lateinit var passwordViewModel: PasswordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricManager = BiometricManager(this)

        val passwordDataStore = PasswordDataStore(this)
        val viewModelFactory = PasswordViewModelFactory(passwordDataStore)
        passwordViewModel = ViewModelProvider(this, viewModelFactory)[PasswordViewModel::class.java]

        biometricManager.setupBiometricAuthentication(
            onAuthenticationSuccess = {
                runOnUiThread {
                    setContent {
                        PasswordManagerApp(passwordViewModel = passwordViewModel)
                    }
                }
            },
            onAuthenticationError = { _, errString ->  // Promijenili smo errorCode u _
                runOnUiThread {
                    Toast.makeText(this, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        )

        setContent {
            AuthenticationScreen(onAuthenticateClick = {
                biometricManager.showBiometricPrompt()
            })
        }
    }
}

@Composable
fun AuthenticationScreen(onAuthenticateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onAuthenticateClick,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Authenticate to Access Passwords")
        }
    }
}

@Composable
fun PasswordManagerApp(
    viewModel: PasswordViewModel = viewModel(),
    passwordViewModel: PasswordViewModel
) {
    var website by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordLength by remember { mutableFloatStateOf(12f) }
    var searchQuery by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var editingPasswordItem by remember { mutableStateOf<PasswordItem?>(null) }

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
                if (isEditing && editingPasswordItem != null) {
                    passwordViewModel.updatePassword(
                        PasswordItem(
                            id = editingPasswordItem!!.id,
                            website = website,
                            username = username,
                            password = password
                        )
                    )
                    isEditing = false
                    editingPasswordItem = null
                } else {
                    passwordViewModel.addPassword(website, username, password)
                }
                website = ""
                username = ""
                password = ""
            }) {
                Text(if (isEditing) "Update Password" else "Add Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val filteredPasswords = viewModel.passwordList.filter {
                    it.website.contains(searchQuery, ignoreCase = true) ||
                            it.username.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    items(filteredPasswords) { passwordItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Website: ${passwordItem.website}")
                                Text("Username: ${passwordItem.username}")
                                Text("Password: ${passwordItem.password}")
                            }

                            Row {
                                IconButton(onClick = {
                                    website = passwordItem.website
                                    username = passwordItem.username
                                    password = passwordItem.password
                                    isEditing = true
                                    editingPasswordItem = passwordItem
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                IconButton(onClick = {
                                    viewModel.deletePassword(passwordItem)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




