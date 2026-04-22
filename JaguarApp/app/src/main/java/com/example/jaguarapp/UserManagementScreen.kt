package com.example.jaguarapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    users: List<User>,
    onUserClick: (User) -> Unit,
    onDeleteUser: (User) -> Unit,
    onAddUser: () -> Unit,
    onBack: () -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var selectedUserForAction by remember { mutableStateOf<User?>(null) }
    var actionType by remember { mutableStateOf("") } // "edit" o "delete"
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de Usuarios", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AddUserButton(onClick = onAddUser)
            }

            if (users.isEmpty()) {
                item { EmptyUsersPlaceholder() }
            } else {
                items(users) { user ->
                    UserItem(
                        user = user,
                        onClick = { 
                            if (!user.isPublic) {
                                selectedUserForAction = user
                                actionType = "edit"
                                showPasswordDialog = true
                            } else {
                                onUserClick(user)
                            }
                        },
                        onDelete = { 
                            if (!user.isPublic) {
                                selectedUserForAction = user
                                actionType = "delete"
                                showPasswordDialog = true
                            } else {
                                onDeleteUser(user)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showPasswordDialog && selectedUserForAction != null) {
        ManagementPasswordDialog(
            userAlias = selectedUserForAction?.alias ?: "",
            correctPassword = selectedUserForAction?.password ?: "",
            passwordInput = passwordInput,
            passwordError = passwordError,
            onPasswordChange = {
                passwordInput = it
                passwordError = false
            },
            onConfirm = {
                val user = selectedUserForAction!!
                showPasswordDialog = false
                selectedUserForAction = null
                passwordInput = ""
                if (actionType == "edit") onUserClick(user) else onDeleteUser(user)
            },
            onError = { passwordError = true },
            onDismiss = {
                showPasswordDialog = false
                selectedUserForAction = null
                passwordInput = ""
                passwordError = false
            }
        )
    }
}

@Composable
private fun AddUserButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text("NUEVO PERFIL", fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EmptyUsersPlaceholder() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No hay usuarios registrados", color = Color.Gray)
    }
}

@Composable
fun UserItem(user: User, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.imageUri ?: User.DEFAULT_AVATAR,
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name.ifBlank { "Sin nombre" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = user.alias.ifBlank { "usuario" }.let { "@$it" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!user.isPublic) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ManagementPasswordDialog(
    userAlias: String,
    correctPassword: String,
    passwordInput: String,
    passwordError: Boolean,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onError: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Acceso Protegido") },
        text = {
            Column {
                Text("Ingresa la contraseña para gestionar a @$userAlias")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError,
                    supportingText = {
                        if (passwordError) {
                            Text("Contraseña incorrecta", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (passwordInput == correctPassword) onConfirm() else onError() }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
