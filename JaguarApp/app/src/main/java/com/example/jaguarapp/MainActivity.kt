package com.example.jaguarapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.jaguarapp.ui.theme.JaguarAppTheme
import kotlin.collections.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemInDarkTheme) }

            // Estado global para navegación y datos
            var currentScreen by remember { mutableStateOf("welcome") }
            val userListState = remember { mutableStateOf(listOf<User>()) }
            var selectedUser by remember { mutableStateOf<User?>(null) }
            
            val userList = userListState.value
            val hasUsers = userList.isNotEmpty()
            
            // Usuario actual para la pantalla de bienvenida (el último editado o creado)
            val activeUser = selectedUser ?: userList.lastOrNull() ?: User()

            JaguarAppTheme(darkTheme = darkTheme) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "welcome" -> WelcomeScreen(
                            users = userListState.value,
                            activeUser = activeUser,
                            isUserRegistered = hasUsers,
                            onUserSelected = { selectedUser = it },
                            onLogin = { currentScreen = "home" },
                            onRegister = {
                                selectedUser = null
                                currentScreen = "profile"
                            },
                            onNavigateToManagement = { currentScreen = "management" }
                        )
                        "home" -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Página Home de la aplicación", style = MaterialTheme.typography.headlineLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sorry bro app still under development",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { currentScreen = "welcome" }) {
                                    Text("Cerrar Sesión")
                                }
                            }
                        }
                        "management" -> UserManagementScreen(
                            users = userList,
                            onUserClick = { user ->
                                selectedUser = user
                                currentScreen = "profile"
                            },
                            onDeleteUser = { user ->
                                userListState.value = userList.filter { it.id != user.id }
                                if (selectedUser?.id == user.id) selectedUser = null
                            },
                            onAddUser = {
                                selectedUser = null
                                currentScreen = "profile"
                            },
                            onBack = { currentScreen = "welcome" }
                        )
                        "profile" -> {
                            val userToEdit = selectedUser ?: remember { User() }
                            var tempUser by remember(userToEdit.id) { mutableStateOf(userToEdit) }
                            
                            ProfileScreen(
                                title = "Mi Perfil",
                                darkTheme = darkTheme,
                                onThemeChange = { darkTheme = it },
                                user = tempUser,
                                onUserChange = { tempUser = it },
                                onSave = { 
                                    // Guardar o actualizar usuario
                                    val index = userList.indexOfFirst { it.id == tempUser.id }
                                    if (index != -1) {
                                        val newList = userList.toMutableList()
                                        newList[index] = tempUser
                                        userListState.value = newList
                                    } else {
                                        userListState.value = userList + tempUser
                                    }
                                    selectedUser = tempUser
                                    currentScreen = "welcome"
                                },
                                onCancel = {
                                    currentScreen = "management"
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    JaguarAppTheme {
        Greeting("Android")
    }
}
