package com.myapp.passwordmanager

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PasswordListScreen(
    viewModel: PasswordViewModel,
    onEditPassword: (PasswordItem) -> Unit
) {
    val passwordList = viewModel.passwordList

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(passwordList) { passwordItem ->
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
                        onEditPassword(passwordItem)
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