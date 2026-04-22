package com.example.jaguarapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    title: String = "Mi Perfil",
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    user: User,
    onUserChange: (User) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    // ESTADOS (Manejan la interactividad)
    var isSaving by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var displayedMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onUserChange(user.copy(imageUri = it)) } }
    )

    // Animación de escala para la imagen de perfil al cambiar
    val imageScale by animateFloatAsState(
        targetValue = if (user.imageUri != null) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ImageScale"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { onThemeChange(!darkTheme) }) {
                        AnimatedContent(
                            targetState = darkTheme,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                            },
                            label = "ThemeIconTransition"
                        ) { isDark ->
                            Icon(
                                imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Cambiar modo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // MENSAJE ANIMADO
            AnimatedVisibility(
                visible = showMessage,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer 
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = displayedMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // SECCIÓN DE IMAGEN DE PERFIL
            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.scale(imageScale)) {
                Surface(
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(12.dp, CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = user.imageUri ?: "https://cdn-icons-png.flaticon.com/512/3135/3135715.png",
                        contentDescription = "Foto de Perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )
                }
                
                // Botón Flotante de Cámara
                SmallFloatingActionButton(
                    onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CAMPOS DE ENTRADA
            ProfileInputField(
                value = user.name,
                onValueChange = { onUserChange(user.copy(name = it)) },
                label = "Nombre Completo",
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInputField(
                value = user.alias,
                onValueChange = { onUserChange(user.copy(alias = it)) },
                label = "Alias / Nombre de usuario",
                icon = Icons.Default.AlternateEmail
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileInputField(
                value = user.email,
                onValueChange = { onUserChange(user.copy(email = it)) },
                label = "Correo Electrónico",
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = user.bio,
                onValueChange = { if (it.length <= 150) onUserChange(user.copy(bio = it)) },
                label = { Text("Biografía") },
                placeholder = { Text("Cuéntanos algo sobre ti...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                supportingText = { Text("${user.bio.length}/150") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CONFIGURACIÓN DE PRIVACIDAD
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Perfil Público", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Permitir que otros usuarios vean tu información.", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = user.isPublic,
                        onCheckedChange = { onUserChange(user.copy(isPublic = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÓN DE ACCIÓN PRINCIPAL
            Button(
                onClick = {
                    if (user.name.isNotBlank()) {
                        scope.launch {
                            isSaving = true
                            delay(1500) // Simulación de red
                            isSaving = false
                            displayedMessage = "¡Perfil actualizado correctamente!"
                            showMessage = true
                            delay(2000)
                            showMessage = false
                            onSave() // Volver a la bienvenida después de guardar
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(if (!isSaving) 8.dp else 0.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isSaving && user.name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                AnimatedContent(targetState = isSaving, label = "ButtonContent") { saving ->
                    if (saving) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Guardando...", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}
