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
            var name by remember { mutableStateOf("") }
            var alias by remember { mutableStateOf("") }
            var email by remember { mutableStateOf("") }
            var bio by remember { mutableStateOf("") }
            var isPublic by remember { mutableStateOf(true) }
            var imageUri by remember { mutableStateOf<Uri?>(null) }

            JaguarAppTheme(darkTheme = darkTheme) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        "welcome" -> WelcomeScreen(
                            name = name,
                            imageUri = imageUri,
                            onNavigateToProfile = { currentScreen = "profile" }
                        )
                        "profile" -> ProfileScreen(
                            darkTheme = darkTheme,
                            onThemeChange = { darkTheme = it },
                            name = name,
                            onNameChange = { name = it },
                            alias = alias,
                            onAliasChange = { alias = it },
                            email = email,
                            onEmailChange = { email = it },
                            bio = bio,
                            onBioChange = { bio = it },
                            isPublic = isPublic,
                            onIsPublicChange = { isPublic = it },
                            imageUri = imageUri,
                            onImageChange = { imageUri = it },
                            onBack = { currentScreen = "welcome" }
                        )
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