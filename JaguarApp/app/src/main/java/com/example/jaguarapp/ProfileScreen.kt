package com.example.jaguarapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    title: String = "Nuevo Perfil",
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    user: User,
    onUserChange: (User) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showMessage by remember { mutableStateOf(false) }
    var displayedMessage by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { onUserChange(user.copy(imageUri = it)) } }
    )

    Scaffold(
        topBar = {
            ProfileTopBar(
                title = title,
                darkTheme = darkTheme,
                onBack = onCancel,
                onThemeToggle = { onThemeChange(!darkTheme) }
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

            SuccessMessage(visible = showMessage, message = displayedMessage)

            ProfileImageSection(
                imageUri = user.imageUri,
                onPickImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfileFormFields(user, onUserChange)

            Spacer(modifier = Modifier.height(24.dp))

            PrivacySettingsSection(user, onUserChange)

            Spacer(modifier = Modifier.height(40.dp))

            SaveButton(
                user = user,
                isSaving = isSaving,
                onSave = {
                    scope.launch {
                        isSaving = true
                        delay(1500)
                        isSaving = false
                        displayedMessage = "¡Perfil guardado correctamente!"
                        showMessage = true
                        delay(2000)
                        showMessage = false
                        onSave()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTopBar(
    title: String,
    darkTheme: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.ExtraBold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = {
            IconButton(onClick = onThemeToggle) {
                AnimatedContent(targetState = darkTheme, label = "ThemeToggle") { isDark ->
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
private fun SuccessMessage(visible: Boolean, message: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = message, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ProfileImageSection(imageUri: Any?, onPickImage: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (imageUri != null) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ImageScale"
    )

    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.scale(scale)) {
        Surface(
            modifier = Modifier
                .size(150.dp)
                .shadow(12.dp, CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
            shape = CircleShape
        ) {
            AsyncImage(
                model = imageUri ?: User.DEFAULT_AVATAR,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clickable { onPickImage() },
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
        }
        SmallFloatingActionButton(
            onClick = onPickImage,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier.offset(x = (-4).dp, y = (-4).dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ProfileFormFields(user: User, onUserChange: (User) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileInputField(
            value = user.name,
            onValueChange = { onUserChange(user.copy(name = it)) },
            label = "Nombre Completo",
            icon = Icons.Default.Person
        )
        ProfileInputField(
            value = user.alias,
            onValueChange = { onUserChange(user.copy(alias = it)) },
            label = "Alias / Nombre de usuario",
            icon = Icons.Default.AlternateEmail
        )
        ProfileInputField(
            value = user.email,
            onValueChange = { onUserChange(user.copy(email = it)) },
            label = "Correo Electrónico",
            icon = Icons.Default.Email
        )
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
    }
}

@Composable
private fun PrivacySettingsSection(user: User, onUserChange: (User) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (user.isPublic) "Perfil Público" else "Perfil Privado",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (user.isPublic) "Libre acceso sin contraseña." 
                               else "Se requerirá contraseña para entrar.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(checked = user.isPublic, onCheckedChange = { onUserChange(user.copy(isPublic = it)) })
            }

            AnimatedVisibility(
                visible = !user.isPublic,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = user.password,
                        onValueChange = { onUserChange(user.copy(password = it)) },
                        label = { Text("Establecer Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(icon, null) }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SaveButton(user: User, isSaving: Boolean, onSave: () -> Unit) {
    val isFormValid = user.isValid()
    
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(if (!isSaving && isFormValid) 8.dp else 0.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        enabled = !isSaving,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            contentColor = if (isFormValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        )
    ) {
        AnimatedContent(targetState = isSaving, label = "SaveButtonContent") { saving ->
            if (saving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 3.dp)
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
}

@Composable
fun ProfileInputField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
