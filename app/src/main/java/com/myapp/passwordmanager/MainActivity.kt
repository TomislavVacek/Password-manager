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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add
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
                        PasswordManagerApp(passwordViewModel = passwordViewModel) // Ovdje pozivamo PasswordManagerApp
                    }
                }
            },
            onAuthenticationError = { _, errString ->
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
    passwordViewModel: PasswordViewModel
) {
    var website by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val passwordLength by remember { mutableFloatStateOf(12f) }
    var searchQuery by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(false) }
    var editingPasswordItem by remember { mutableStateOf<PasswordItem?>(null) }

    var selectedScreen by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Password Manager") })
        },
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Password") },
                    label = { Text("Add") },
                    selected = selectedScreen == 0,
                    onClick = { selectedScreen = 0 }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "View Passwords") },
                    label = { Text("Passwords") },
                    selected = selectedScreen == 1,
                    onClick = { selectedScreen = 1 }
                )
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
            when (selectedScreen) {
                0 -> {
                    NewPasswordScreen(
                        viewModel = passwordViewModel,
                        website = website,
                        username = username,
                        password = password,
                        passwordLength = passwordLength,
                        isEditing = isEditing,
                        editingPasswordItem = editingPasswordItem,
                        onPasswordAdded = {
                            website = ""
                            username = ""
                            password = ""
                            isEditing = false
                            editingPasswordItem = null
                        }
                    )
                }
                1 -> {
                    PasswordListScreen(
                        viewModel = passwordViewModel,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        onEditPassword = { passwordItem ->
                            website = passwordItem.website
                            username = passwordItem.username
                            password = passwordItem.password
                            isEditing = true
                            editingPasswordItem = passwordItem
                            selectedScreen = 0 // Prebaci se na ekran za uređivanje
                        }
                    )
                }
            }
        }
    }
}

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
    }) {
        Text(if (isEditing) "Update Password" else "Add Password")
    }
}

@Composable
fun PasswordListScreen(
    viewModel: PasswordViewModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditPassword: (PasswordItem) -> Unit
) {
    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChange(it) },
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
                            onEditPassword(passwordItem)
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






