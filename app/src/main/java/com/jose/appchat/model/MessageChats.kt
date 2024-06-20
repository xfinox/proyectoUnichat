package com.jose.appchat.model

data class MessageChats(
    var messageId: String = "",   // ID único del mensaje
    val text: String = "",        // Contenido del mensaje
    val senderId: String = "",    // ID del remitente del mensaje
    val receiverId: String = "",  // ID del receptor del mensaje
    val time: Long = 0            // Marca de tiempo del mensaje (en milisegundos desde 1970-01-01T00:00:00Z)
) {
    // Constructor adicional para manejar la creación de mensajes con un ID específico
    constructor(
        text: String,
        senderId: String,
        receiverId: String,
        time: Long,
    ) : this("",text, senderId, receiverId, time)
}
