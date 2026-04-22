package com.example.jaguarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jaguarapp.ui.theme.JaguarAppTheme

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*

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
                            onNavigateToProfile = { 
                                selectedUser = activeUser
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
                                if (selectedUser?.id == user.id) {
                                    selectedUser = null
                                }
                            },
                            onAddUser = {
                                selectedUser = null
                                currentScreen = "profile"
                            },
                            onBack = { currentScreen = "welcome" }
                        )
                        "profile" -> {
                            val userToEdit = selectedUser ?: User()
                            var tempUser by remember(userToEdit.id) { mutableStateOf(userToEdit) }
                            
                            ProfileScreen(
                                darkTheme = darkTheme,
                                onThemeChange = { darkTheme = it },
                                name = tempUser.name,
                                onNameChange = { tempUser = tempUser.copy(name = it) },
                                alias = tempUser.alias,
                                onAliasChange = { tempUser = tempUser.copy(alias = it) },
                                email = tempUser.email,
                                onEmailChange = { tempUser = tempUser.copy(email = it) },
                                bio = tempUser.bio,
                                onBioChange = { tempUser = tempUser.copy(bio = it) },
                                isPublic = tempUser.isPublic,
                                onIsPublicChange = { tempUser = tempUser.copy(isPublic = it) },
                                imageUri = tempUser.imageUri,
                                onImageChange = { tempUser = tempUser.copy(imageUri = it) },
                                onBack = { 
                                    // Guardar o actualizar usuario
                                    val index = users.indexOfFirst { it.id == tempUser.id }
                                    if (index != -1) {
                                        users = users.toMutableList().apply { set(index, tempUser) }
                                    } else {
                                        users = users + tempUser
                                    }
                                    selectedUser = tempUser
                                    currentScreen = "welcome" 
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