package com.myapp.passwordmanager

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.SaveAlt // Ikona za Backup
import androidx.compose.material.icons.filled.Restore // Ikona za Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myapp.passwordmanager.QRCodeUtils.generateQRCode
import androidx.compose.material.icons.filled.SaveAlt // Ikona za Backup
import androidx.compose.material.icons.filled.Restore // Ikona za Restore
import androidx.compose.ui.platform.LocalContext

class MainActivity : FragmentActivity() {
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
    var showSnackbar by remember { mutableStateOf(false) } // Za prikaz Snackbar-a
    var snackbarMessage by remember { mutableStateOf("") } // Poruka za Snackbar
    var showQRCode by remember { mutableStateOf(false) } // Stanje za QR kod
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) } // Za pohranu QR koda

    var selectedScreen by remember { mutableIntStateOf(0) }
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current // Koristi LocalContext.current unutar @Composable

    // Stanja za backup i restore
    var performBackup by remember { mutableStateOf(false) }
    var performRestore by remember { mutableStateOf(false) }

    // Ako je potrebno izvršiti backup, pokreni efekt
    if (performBackup) {
        LaunchedEffect(performBackup) {
            passwordViewModel.backupPasswords(context)
            snackbarMessage = "Backup completed successfully!"
            showSnackbar = true
            performBackup = false
        }
    }

    // Ako je potrebno izvršiti restore, pokreni efekt
    if (performRestore) {
        LaunchedEffect(performRestore) {
            passwordViewModel.restorePasswords(context)
            snackbarMessage = "Passwords restored successfully!"
            showSnackbar = true
            performRestore = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Password Manager") },
                actions = {
                    // Dodaj Backup ikonu
                    IconButton(onClick = {
                        performBackup = true // Pokreni backup
                    }) {
                        Icon(Icons.Default.SaveAlt, contentDescription = "Backup Passwords")
                    }

                    // Dodaj Restore ikonu
                    IconButton(onClick = {
                        performRestore = true // Pokreni restore
                    }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore Passwords")
                    }
                }
            )
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

                            // Pokaži obavijest o uspešnom dodavanju
                            snackbarMessage = if (isEditing) "Password successfully updated!" else "Password successfully added!"
                            showSnackbar = true
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
                        },
                        onGenerateQRCode = { encryptedQRCodeData ->
                            qrCodeBitmap = QRCodeUtils.generateQRCode(encryptedQRCodeData) // Generiraj QR kod
                            showQRCode = true // Prikaži QR kod
                        }
                    )
                }
            }
        }

        // Prikaz QR koda
        if (showQRCode && qrCodeBitmap != null) {
            AlertDialog(
                onDismissRequest = { showQRCode = false },
                title = { Text("QR Code") },
                text = {
                    qrCodeBitmap?.let {
                        Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
                    }
                },
                confirmButton = {
                    Button(onClick = { showQRCode = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Prikaz Snackbar-a za obaveštenja
        if (showSnackbar) {
            LaunchedEffect(scaffoldState.snackbarHostState) {
                scaffoldState.snackbarHostState.showSnackbar(snackbarMessage)
                showSnackbar = false
            }
        }
    }
}
