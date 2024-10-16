package com.myapp.passwordmanager

import android.os.Bundle
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val passwordDataStore = PasswordDataStore(this)
        val viewModelFactory = PasswordViewModelFactory(passwordDataStore)
        val passwordViewModel: PasswordViewModel = ViewModelProvider(this, viewModelFactory)[PasswordViewModel::class.java]


        setContent {
            PasswordManagerApp(passwordViewModel = passwordViewModel)
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

    // Držimo stanje pretrage
    var searchQuery by remember { mutableStateOf("") }

    // Stanje za praćenje koja lozinka se uređuje
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

            // Gumb za generiranje lozinke
            Button(onClick = {
                password = PasswordGenerator.generatePassword(passwordLength.toInt()) // Generiraj lozinku na temelju duljine
            }) {
                Text("Generate Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dodaj ili ažuriraj lozinku u pohranu
            Button(onClick = {
                if (isEditing && editingPasswordItem != null) {
                    // Ako uređujemo, ažuriramo postojeću lozinku
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
                    // Ako ne uređujemo, dodajemo novu lozinku
                    passwordViewModel.addPassword(website, username, password)
                }
                website = ""
                username = ""
                password = ""
            }) {
                Text(if (isEditing) "Update Password" else "Add Password")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar i lista unesenih lozinki prebaceni na dno aplikacije
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Ostatak prostora koristi za listu i pretragu
            ) {
                // Polje za pretragu lozinki
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Prikazujemo samo lozinke koje odgovaraju upitu pretrage
                val filteredPasswords = viewModel.passwordList.filter {
                    it.website.contains(searchQuery, ignoreCase = true) ||
                            it.username.contains(searchQuery, ignoreCase = true)
                }

                // LazyColumn za skrolanje liste lozinki
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight() // Omogućuje više prostora za prikaz lozinki
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
                                // Gumb za uređivanje
                                IconButton(onClick = {
                                    // Postavimo stanje za uređivanje
                                    website = passwordItem.website
                                    username = passwordItem.username
                                    password = passwordItem.password
                                    isEditing = true
                                    editingPasswordItem = passwordItem
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }

                                // Gumb za brisanje
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




