package com.example.jaguarapp

import android.net.Uri
import java.util.UUID

/**
 * Data class representing a User in the JaguarApp.
 * @property id Unique identifier for the user.
 * @property name Full name of the user.
 * @property alias Display name/username.
 * @property email User's email address.
 * @property bio Short biography.
 * @property isPublic Whether the profile is public or protected by password.
 * @property password Password required for private profiles.
 * @property imageUri URI to the user's profile image.
 */
data class User(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val alias: String = "",
    val email: String = "",
    val bio: String = "",
    val isPublic: Boolean = true,
    val password: String = "",
    val imageUri: Uri? = null
) {
    // Helper to determine if user info is sufficient for saving
    fun isValid(): Boolean {
        val basicInfoValid = name.isNotBlank() && alias.isNotBlank()
        return if (isPublic) basicInfoValid else basicInfoValid && password.isNotBlank()
    }

    // Default image URL for placeholders
    companion object {
        const val DEFAULT_AVATAR = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png"
    }
}
