package com.jose.appchat.model

data class Contact_group(
    var id: String = "",
    val nombre: String = "",
    val email: String = "",
    val profilePicturePath: String = "",
    val userId: String = "",
    var isSelected: Boolean = false // Añadir esta propiedad
)
