package com.myapp.passwordmanager

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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
        // Polje za pretragu lozinki
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { onSearchQueryChange(it) },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filtrirane lozinke na temelju pretrage
        val filteredPasswords = viewModel.passwordList.filter {
            it.website.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true)
        }

        // Lista lozinki
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
                    // Prikaz web stranice, korisničkog imena i lozinke
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
                            // Opcija za uređivanje lozinke
                            DropdownMenuItem(onClick = {
                                onEditPassword(passwordItem)
                                expanded = false
                            }) {
                                Text("Edit")
                            }

                            // Opcija za brisanje lozinke
                            DropdownMenuItem(onClick = {
                                viewModel.deletePassword(passwordItem)
                                expanded = false
                            }) {
                                Text("Delete")
                            }

                            // Opcija za generiranje enkriptiranog QR koda
                            DropdownMenuItem(onClick = {
                                // Generiraj tajni ključ
                                val secretKey = EncryptionUtils.generateSecretKey()
                                // Enkriptiraj podatke
                                val encryptedData = EncryptionUtils.encrypt(
                                    """
                                    {
                                        "website": "${passwordItem.website}",
                                        "username": "${passwordItem.username}",
                                        "password": "${passwordItem.password}"
                                    }
                                    """.trimIndent(),
                                    secretKey
                                )

                                // QR kod sadrži enkriptirani tekst
                                val qrCodeData = encryptedData.first
                                onGenerateQRCode(qrCodeData)
                                expanded = false
                            }) {
                                Text("Share via Encrypted QR Code")
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ShowQRCodeScreen(qrCodeBitmap: Bitmap?) {
    qrCodeBitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "QR Code",
            modifier = Modifier.size(200.dp)
        )
    } ?: run {
        Text("Failed to generate QR code")
    }
}
