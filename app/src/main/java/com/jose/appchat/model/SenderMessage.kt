package com.jose.appchat.model

data class SenderMessage(
    var id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfileUrl: String = "",
    val timestamp: Long = 0
)
