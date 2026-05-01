package com.kal.portfolio.vaultapp.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kal.portfolio.vaultapp.data.crypto.EncryptedData
import com.kal.portfolio.vaultapp.domain.model.VaultEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as FragmentActivity

    uiState.securityError?.let { error ->
        SecurityErrorScreen(message = error)
        return
    }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vault") },
                actions = {
                    IconButton(onClick = { viewModel.lockVault() }) {
                        Icon(Icons.Default.Lock, contentDescription = "Verrouiller")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun secret stocké",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        VaultEntryCard(
                            entry = entry,
                            decryptedValue = uiState.decryptedValues[entry.id],
                            onDelete = { viewModel.deleteEntry(entry.id) },
                            viewModel = viewModel,
                            activity = activity
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            onDismiss = { },
            onConfirm = { label, value ->
                val cipher = viewModel.getEncryptCipher()
                BiometricHelper(
                    activity = activity,
                    onSuccess = { unlockedCipher ->
                        viewModel.saveEntry(label, value, unlockedCipher)
                    },
                    onError = { }
                ).authenticate(cipher)
            }
        )
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
fun VaultEntryCard(
    entry: VaultEntry,
    decryptedValue: String?,
    onDelete: () -> Unit,
    viewModel: VaultViewModel,
    activity: FragmentActivity
) {
    var showValue by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.titleMedium
                )
                if (decryptedValue != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (showValue) decryptedValue else "••••••••",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row {
                if (decryptedValue != null) {
                    IconButton(onClick = { showValue = !showValue }) {
                        Icon(Icons.Default.Visibility, contentDescription = "Afficher")
                    }
                } else {
                    IconButton(onClick = {
                        val encryptedData = EncryptedData.fromBase64(entry.encryptedValue)
                        val cipher = viewModel.getDecryptCipher(encryptedData.iv)
                        BiometricHelper(
                            activity = activity,
                            onSuccess = { unlockedCipher ->
                                viewModel.decryptEntry(entry, unlockedCipher)
                            },
                            onError = {}
                        ).authenticate(cipher)
                    }) {
                        Icon(Icons.Default.Lock, contentDescription = "Déchiffrer")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
            }
        }
    }
}

@Composable
fun AddEntryDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, value: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var showValue by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau secret") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Secret") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Unspecified,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Unspecified,
                        platformImeOptions = null,
                        showKeyboardOnFocus = null,
                        hintLocales = null
                    ),
                    visualTransformation = if (showValue)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showValue = !showValue }) {
                            Icon(Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (label.isNotBlank() && value.isNotBlank()) onConfirm(label, value) },
                enabled = label.isNotBlank() && value.isNotBlank()
            ) {
                Text("Sauvegarder")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun SecurityErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Accès refusé",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}