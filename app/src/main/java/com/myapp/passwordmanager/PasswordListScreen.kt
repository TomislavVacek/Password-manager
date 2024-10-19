package com.myapp.passwordmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PasswordListScreen(
    viewModel: PasswordViewModel,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditPassword: (PasswordItem) -> Unit,
    onGenerateQRCode: (String) -> Unit
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

                    // Overflow menu sa 3 tačke
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                onEditPassword(passwordItem)
                                expanded = false
                            }) {
                                Text("Edit")
                            }

                            DropdownMenuItem(onClick = {
                                viewModel.deletePassword(passwordItem)
                                expanded = false
                            }) {
                                Text("Delete")
                            }

                            DropdownMenuItem(onClick = {
                                onGenerateQRCode(passwordItem.password)
                                expanded = false
                            }) {
                                Text("Share via QR Code")
                            }
                        }
                    }
                }
            }
        }
    }
}
