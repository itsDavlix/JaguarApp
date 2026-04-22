package com.example.jaguarapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
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
    // Estados para el diálogo de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var selectedUserForAction by remember { mutableStateOf<User?>(null) }
    var actionType by remember { mutableStateOf("") } // "edit" o "delete"
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gestión de usuario", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = onAddUser,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(bottom = 8.dp),
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

            if (users.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay usuarios registrados", color = Color.Gray)
                    }
                }
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

    // Diálogo de Validación de Contraseña
    if (showPasswordDialog && selectedUserForAction != null) {
        AlertDialog(
            onDismissRequest = { 
                showPasswordDialog = false
                selectedUserForAction = null
                passwordInput = ""
                passwordError = false
            },
            title = { Text("Acceso Protegido") },
            text = {
                Column {
                    Text("Ingresa la contraseña para gestionar a @${selectedUserForAction?.alias}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { 
                            passwordInput = it
                            passwordError = false
                        },
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
                Button(
                    onClick = {
                        if (passwordInput == selectedUserForAction?.password) {
                            val user = selectedUserForAction!!
                            showPasswordDialog = false
                            selectedUserForAction = null
                            passwordInput = ""
                            if (actionType == "edit") {
                                onUserClick(user)
                            } else if (actionType == "delete") {
                                onDeleteUser(user)
                            }
                        } else {
                            passwordError = true
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPasswordDialog = false
                    selectedUserForAction = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.imageUri ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                error = painterResource(id = android.R.drawable.ic_menu_report_image)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { "Sin nombre" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = user.alias.ifBlank { "usuario" }.let { "@$it" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!user.isPublic) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Protegido",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
