package com.example.jaguarapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jaguarapp.ui.theme.JaguarAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemInDarkTheme) }

            // Estado global para navegación y datos
            var currentScreen by remember { mutableStateOf("welcome") }
            var users by remember { mutableStateOf(listOf<User>()) }
            var selectedUser by remember { mutableStateOf<User?>(null) }
            
            // Usuario actual para la pantalla de bienvenida (el último editado o creado)
            val activeUser = selectedUser ?: users.lastOrNull() ?: User()

            JaguarAppTheme(darkTheme = darkTheme) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "welcome" -> WelcomeScreen(
                            alias = activeUser.alias,
                            imageUri = activeUser.imageUri,
                            onNavigateToNewProfile = { 
                                selectedUser = null
                                currentScreen = "profile"
                            },
                            onNavigateToManagement = { currentScreen = "management" }
                        )
                        "management" -> UserManagementScreen(
                            users = users,
                            onUserClick = { user ->
                                selectedUser = user
                                currentScreen = "profile"
                            },
                            onDeleteUser = { user ->
                                users = users.filter { it.id != user.id }
                                if (selectedUser?.id == user.id) selectedUser = null
                            },
                            onBack = { currentScreen = "welcome" }
                        )
                        "profile" -> {
                            val userToEdit = selectedUser ?: remember { User() }
                            var tempUser by remember(userToEdit.id) { mutableStateOf(userToEdit) }
                            
                            ProfileScreen(
                                title = if (selectedUser == null) "Nuevo Perfil" else "Mi Perfil",
                                darkTheme = darkTheme,
                                onThemeChange = { darkTheme = it },
                                user = tempUser,
                                onUserChange = { tempUser = it },
                                onSave = { 
                                    // Guardar o actualizar usuario
                                    val index = users.indexOfFirst { it.id == tempUser.id }
                                    users = if (index != -1) {
                                        users.toMutableList().apply { set(index, tempUser) }
                                    } else {
                                        users + tempUser
                                    }
                                    selectedUser = tempUser
                                    currentScreen = "management" 
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
