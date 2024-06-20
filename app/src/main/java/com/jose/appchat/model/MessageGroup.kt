package com.jose.appchat.model

data class MessageGroup(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileUrl: String = "", // Nueva URL para la foto de perfil del remitente
    val text: String = "",
    val timestamp: Long = 0
)
