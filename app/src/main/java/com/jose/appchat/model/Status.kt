package com.jose.appchat.model


data class Status(
    val id: String = "",
    val imageUrl: String = "",
    val timestamp: Long = 0,
    val userId: String = "" // Agrega el userId aquí
)
