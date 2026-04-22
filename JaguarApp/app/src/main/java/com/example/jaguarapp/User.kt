package com.example.jaguarapp

import android.net.Uri
import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val alias: String = "",
    val email: String = "",
    val bio: String = "",
    val isPublic: Boolean = true,
    val imageUri: Uri? = null
)
