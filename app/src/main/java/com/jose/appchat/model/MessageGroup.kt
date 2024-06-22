package com.jose.appchat.model

data class MessageGroup(
    var id: String = "",
    val senderId: String = "",
    var senderName: String = "",
    var senderProfileUrl: String = "", // Nueva URL para la foto de perfil del remitente
    val text: String = "",
    val timestamp: Long = 0
)
