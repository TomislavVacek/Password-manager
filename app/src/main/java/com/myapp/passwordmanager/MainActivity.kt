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






